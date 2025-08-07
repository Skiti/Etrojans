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

import java.util.Arrays;

import io.reactivex.rxjava3.functions.Consumer;

public class AuthRegister extends AuthBase {

    private final Consumer<Boolean> onComplete;

    public AuthRegister(IDevice device, DataRegister data, Consumer<Boolean> onComplete) {
        super(device, data);
        this.onComplete = onComplete;
    }

    @Override
    protected void handleMessage(byte[] message) throws Throwable {
       System.out.println("register: handling message - " + Util.bytesToHex(message));

        if (!data.hasRemoteInfo()) {
            updateProgress("register: handling remote info (3/9)");
            if (Arrays.equals(message, CommandRegister.SendingCt)) {
                write(MiUUID.AVDTP, CommandLogin.ReceiveReady);
            } else {
                write(MiUUID.AVDTP, CommandLogin.Received, complete ->
                        write(MiUUID.AVDTP, CommandRegister.SendingKey));
                write(MiUUID.UPNP, CommandRegister.KeyExchange);

                updateProgress("register: remote info received (4/9)");
                if (!data.setRemoteInfo(message)) {
                    stopNotifyTrigger.onNext(true);
                    updateProgress("register: failed (9/9)");
                    onComplete.accept(false);
                }
            }
        } else if (!data.hasRemoteKey()) {
            updateProgress("register: handling remote key (5/9)");
            if (Arrays.equals(message, CommandLogin.ReceiveReady)) {
                writeParcel(MiUUID.AVDTP, data.getMyKey());
            } else if (Arrays.equals(message, CommandLogin.Received)) {
                updateProgress("register: " + "public key sent (6/9)");
            } else {
                if (Arrays.equals(message, CommandRegister.SendingKey)) {
                    write(MiUUID.AVDTP, CommandLogin.ReceiveReady);
                } else {// if (message.length == data.getMyKey().length){
                    data.setRemoteKey(message);
                    updateProgress("register: " + "remote key received -> calculate (7/9)");
                    data.calculate();
                    write(MiUUID.AVDTP, CommandLogin.Received);
                    write(MiUUID.AVDTP, CommandRegister.SendingCt);
                    updateProgress("register: " + "ct sent (8/9)");
                }
            }
        } else {
            if (Arrays.equals(message, CommandLogin.ReceiveReady)) {
                writeParcel(MiUUID.AVDTP, data.getCt());
            } else if (Arrays.equals(message, CommandLogin.Received)) {
                write(MiUUID.UPNP, CommandRegister.AuthRequest);
            } else if (Arrays.equals(message, CommandRegister.AuthConfirmed)) {
                stopNotifyTrigger.onNext(true);
                //compositeDisposable.dispose();

                updateProgress("register: succeeded (9/9)");
                onComplete.accept(true);
            } else if (Arrays.equals(message, CommandRegister.AuthDenied)) {
                stopNotifyTrigger.onNext(true);
                //compositeDisposable.dispose();

                updateProgress("register: failed (9/9)");
                onComplete.accept(false);
            }
        }
    }

    @Override
    public void exec() {
        super.exec();

        // TODO: improve this
        if (!device.isConnected()) {
            updateProgress("register: connecting (1/9)");
            init(3, onConnect -> {
                updateProgress("register: sending request (2/9)");
                write(MiUUID.UPNP, CommandRegister.GetInfo);
            }, timeout -> onComplete.accept(false));
        } else {
            updateProgress("register: subscribing (1/9)");
            subscribeNotify(3, timeout -> onComplete.accept(false));
            updateProgress("register: sending request (2/9)");
            write(MiUUID.UPNP, CommandRegister.GetInfo);
        }
    }

    public AuthLogin toLogin(DataLogin dataLogin, Consumer<Boolean> onComplete) {
        dispose();
        return new AuthLogin(device, dataLogin, onComplete);
    }
}
