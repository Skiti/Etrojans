//
//  MiAuth - Authenticate and interact with Xiaomi devices over BLE
//  Copyright (C) 2022  Daljeet Nandha
//
//  This program is free software: you can redistribute it and/or modify
//  it under the terms of the GNU Affero General Public License as
//  published by the Free Software Foundation, either version 3 of the
//  License, or (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU Affero General Public License for more details.
//
//  You should have received a copy of the GNU Affero General Public License
//  along with this program.  If not, see <https://www.gnu.org/licenses/>.
//
package com.malicious.scootertoolkit.xiaomi.miauth;

import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Queue;

import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;

public class AuthCommand extends AuthBase {
    private final Queue<Commando> cmdQueue;
    private final Queue<Commando> rcvQueue;
    private volatile boolean bursting = false;

    public AuthCommand(IDevice device, IData data) {
        super(device, data);
        this.cmdQueue = new ArrayDeque<>();
        this.rcvQueue = new ArrayDeque<>();
    }

    @Override
    protected void handleMessage(byte[] message) {
        if (message == null) {
            return;
        }

        updateProgress("command: (2/2) handling message " + Util.bytesToHex(message));

        byte[] dec = data.getParent().decryptUart(message);
        System.out.println("command: decoded response:" + Util.bytesToHex(dec));

        Commando cmd;
        while ((cmd = rcvQueue.poll()) != null) {
            if (((dec[1] == 1 || dec[1] == 2) && dec[2] == cmd.getCommand()[5])
                    || ((dec[1] != 1 && dec[1] != 2) && dec[1] == cmd.getCommand()[4])) {
                byte[] response = Arrays.copyOfRange(dec, 3, dec.length);
                cmd.respond(response);

                break;
            } else {
                cmd.respond(new byte[0]);
            }
        }
    }

    @Override
    public void exec() {
        if (device.isConnected()) {
            final Disposable rxSub = device.onNotify(MiUUID.RX)
                    .takeUntil(stopNotifyTrigger)
                    .subscribe(
                            this::receiveParcel,
                            Throwable::printStackTrace
                    );
            compositeDisposable.add(rxSub);
        } else {
            // TODO
        }
    }

    public void clear() {
        cmdQueue.clear();
        receiveBuffer.clear();
    }

    public void push(Commando cmd) {
        cmdQueue.add(cmd);
    }

    public void push(byte[] cmd, Consumer<byte[]> onResponse) {
        cmdQueue.add(new Commando(cmd, onResponse));
    }

    public void burst(ArrayList<byte[]> cmd, Consumer<Integer> onProgress) {
        ArrayList<byte[]> allCmds = new ArrayList<>();
        for (int i = 0; i < cmd.size(); i++) {
            allCmds.addAll(getChunked(cmd.get(i)));
        }

        for (int i = 0; i < allCmds.size(); i++) {
            final int finalI = (int)((float)i / allCmds.size() * 100);
            write(MiUUID.TX, allCmds.get(i), complete -> {
                onProgress.accept(finalI);
            });
        }
    }

    public boolean isEmpty() {
        return cmdQueue.isEmpty();
    }

    public void sendNext() {
        if (rcvQueue.size() > 10) {
            rcvQueue.poll();
        }

        Commando cmd = cmdQueue.poll();
        if (cmd != null) {
            writeChunked(cmd.getCommand());
            rcvQueue.add(cmd);
        }
    }

    public void handler() {
        byte[] message = null;
        if (receiveBuffer != null && !receiveBuffer.hasRemaining()) {
            message = new byte[receiveBuffer.position()];
            receiveBuffer.position(0);
            receiveBuffer.get(message);
            receiveBuffer.clear();
        }
        handleMessage(message);
    }

    @Override
    protected void receiveParcel(byte[] data) {
        if (data == null || data.length == 0) {
            System.out.println("command: recv empty data");
            return;
        }

        System.out.println("command: recv message "+ Util.bytesToHex(data));
        byte[] dec = this.data.getParent().decryptUart(data);
        System.out.println("command: decoded response:" + Util.bytesToHex(dec));
        if (data.length > 2
                && (data[0] & 0xff) == 0x55
                && ((data[1] & 0xff) == 0xaa || (data[1] & 0xff) == 0xab)
                && (data[2] & 0xff) > 0
        ) {
            int extra_bytes;
            if ((data[1] & 0xff) != 0xaa) {
                extra_bytes = getData().hasIvs() ? 16 : 10;
            } else {
                extra_bytes = 6;
            }
            receiveBuffer = ByteBuffer.allocate(extra_bytes + data[2]);
            receiveBuffer.put(data);
        } else if (receiveBuffer != null) {
            receiveBuffer.put(data);
        }

        //if (!waitTimeout && receiveBuffer != null && !receiveBuffer.hasRemaining()) {
        //    handler();
        //}
    }

    private void writeChunked(byte[] cmd) {
        updateProgress("command: (1/2) sending command " + Util.bytesToHex(cmd));

        byte[] msg = data.getParent().encryptUart(cmd);
        ByteBuffer buf = ByteBuffer.wrap(msg);
        while (buf.remaining() > 0) {
            int len = Math.min(buf.remaining(), ChunkSize+2);
            byte[] chunk = new byte[len];
            buf.get(chunk, 0, len);

            write(MiUUID.TX, chunk);
        }
    }

    private ArrayList<byte[]> getChunked(byte[] cmd) {
        ArrayList<byte[]> chunked = new ArrayList<>();

        byte[] msg = data.getParent().encryptUart(cmd);
        ByteBuffer buf = ByteBuffer.wrap(msg);
        while (buf.remaining() > 0) {
            int len = Math.min(buf.remaining(), ChunkSize+2);
            byte[] chunk = new byte[len];
            buf.get(chunk, 0, len);

            chunked.add(chunk);
        }
        return chunked;
    }
}
