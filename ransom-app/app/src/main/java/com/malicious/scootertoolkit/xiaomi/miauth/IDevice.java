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

import java.util.ArrayList;
import java.util.UUID;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.functions.Consumer;

public interface IDevice {
    void prepare();
    void connect(Consumer<Boolean> onConnect);
    void disconnect();
    boolean isConnected();
    void write(UUID uuid, byte[] data, Consumer<byte[]> onWriteSuccess);

    void write(UUID uuid, ArrayList<byte[]> data, Consumer<byte[]> onWriteSuccess);

    void read(UUID uuid, Consumer<byte[]> onReadSuccess, Consumer<Throwable> onReadFail);
    Observable<byte[]> onNotify(UUID uuid);

    boolean isDisconnected();

    void onDisconnect(Consumer<Boolean> onDisconnect);

    String getName();
}
