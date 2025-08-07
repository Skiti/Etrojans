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
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.subjects.PublishSubject;

public class AuthBase {
    public static int ChunkSize = 18;
    protected IDevice device;
    protected final IData data;
    protected ByteBuffer receiveBuffer;
    protected final CompositeDisposable compositeDisposable = new CompositeDisposable();
    protected final PublishSubject<Boolean> stopNotifyTrigger = PublishSubject.create();

    String progress;
    private Consumer<String> onProgressUpdate;

    public AuthBase(IDevice device, IData data) {
        this.device = device;
        this.data = data;
    }

    public void updateProgress(String p) {
        System.out.println(p);
        progress = p;

        if (onProgressUpdate != null) {
            try {
                onProgressUpdate.accept(progress);
            } catch (Exception e) {
                e.printStackTrace();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void setProgressCallback(Consumer<String> onProgressUpdate) {
        this.onProgressUpdate = onProgressUpdate;
    }

    protected void write(UUID uuid, byte[] data) {
        write(uuid, data, null);
    }

    protected void write(UUID uuid, byte[] data, Consumer<byte[]> onComplete) {
        device.write(uuid, data, resp -> {
            System.out.println("auth: write response " + Util.bytesToHex(resp));
            if (onComplete != null) {
                onComplete.accept(resp);
            }
        });
    }

    protected void writeChunk(UUID uuid, ArrayList<byte[]> data, Consumer<byte[]> onComplete) {
        device.write(uuid, data, resp -> {
            System.out.println("auth: write response " + Util.bytesToHex(resp));
            if (onComplete != null) {
                onComplete.accept(resp);
            }
        });
    }

    protected void writeParcel(UUID uuid, byte[] data) {
        ByteBuffer buf = ByteBuffer.wrap(data);
        for (int i = 1; buf.remaining() > 0; i++) {
            int len = Math.min(buf.remaining(), ChunkSize);
            byte[] chunk = new byte[2 + len];
            chunk[0] = (byte) i;
            chunk[1] = (byte) 0;

            buf.get(chunk, 2, len);
            //final boolean isLast = buf.remaining() == 0;
            write(uuid, chunk);
        }
    }
    protected void subscribeNotify(int timeout, Consumer<Boolean> onTimeout) {
        System.out.println("auth: subscribe");
        final Disposable upnpSub = device.onNotify(MiUUID.UPNP)
                .takeUntil(stopNotifyTrigger)
                .subscribe(
                    this::receiveParcel,
                        Throwable::printStackTrace
        );
        final Disposable avdtpSub = device.onNotify(MiUUID.AVDTP)
                .takeUntil(stopNotifyTrigger)
                .timeout(timeout, TimeUnit.SECONDS, Observable.create(emitter -> {
                    System.out.println("auth: subscription timeout");
                    onTimeout.accept(true);
                    stopNotifyTrigger.onNext(true);
                }))
                .subscribe(
                        this::receiveParcel,
                        Throwable::printStackTrace
                );

        final Disposable stopSub = stopNotifyTrigger.subscribe(
                next -> {
                    System.out.println("auth: subscription stopped");
                    //compositeDisposable.dispose();
        });

        compositeDisposable.add(upnpSub);
        compositeDisposable.add(avdtpSub);
        compositeDisposable.add(stopSub);
    }

    protected void init(int timeout, Consumer<Boolean> callback, Consumer<Boolean> onTimeout) {
        device.prepare();
        device.connect(connect -> {
            subscribeNotify(timeout, onTimeout);

            callback.accept(connect);
        });
    }

    protected void receiveParcel(byte[] data) throws Throwable {
        System.out.println("auth: recv message " + Util.bytesToHex(data));
        int frame = data[0] & 0xff + 0x100 * data[1] & 0xff;
        System.out.println("auth: recv frame " + frame);
        if (frame == 0) {
            if (data.length == 6) {
                receiveBuffer = ByteBuffer.allocate((data[4] & 0xff + 0x100 * data[5] & 0xff) * ChunkSize);
            }
            handleMessage(data);
        } else if (frame > 0x10) {
            handleMessage(data);
        } else {
            receiveBuffer.put(data, 2, data.length - 2);
            if (receiveBuffer.remaining() < ChunkSize) {
                byte[] message = new byte[receiveBuffer.position()];
                receiveBuffer.position(0);
                receiveBuffer.get(message);
                handleMessage(message);
            }
        }
    }

    public void dispose() {
        compositeDisposable.dispose();
    }

    public boolean isDisposed() {
        return compositeDisposable.isDisposed();
    }

    public void onComplete(Consumer<Boolean> complete) {
        Disposable subscribe = stopNotifyTrigger.subscribe(complete);
    }

    public void reset() {
        compositeDisposable.dispose();
        device.disconnect();
        if (data != null) {
            data.clear();
        }
    }

    protected void handleMessage(byte[] message) throws Throwable {
    }

    public void exec() {
    }

    public AuthCommand toCommand() {
        dispose();
        return new AuthCommand(device, data);
    }

    public Data getData() {
        return data.getParent();
    }
}
