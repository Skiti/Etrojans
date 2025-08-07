# E-Trojans

We release our toolkit that reproduces our attacks, reimplements the UART and I2C binary protocols, and offers a ransom payments app with backend.

## Toolkit Description and Goals
E-Trojans is a toolkit that implements five attacks on the Xiaomi M365 and ES3 (Mi3) electric scooters.
We release a set of malicious BCTRL firmware that can be flashed on the e-scooter to alter its behaviour.
For example, it can infect the e-scooter with a ransomware that induces undervoltage to permanently damage the battery.
Each malicious firmware adds multiple malicious capabilities that we implement on the BMS after RE the stock Xiaomi firmware.
The firmware speaks UART and I2C to communicate with the other entities on the UART (i.e., BTS and DRV) and I2C (i.e., BMON) bus.
We also offer a proof-of-concept Android app that simulates ransom payment and its backend server, that includes a database and two web APIs.
The toolkit works with minimal resources: a computer that supports BLE, Python (+ libraries), Java (+ libraries),  MongoDB, Django, an Android phone (no root required), and a Xiaomi e-scooter (M365 or ES3).

## Attack Demo
We show a video demonstration of our malicious firmware's capabilities.
In this scenario, we perform User Tracking via Internals (UTI) and Denial of E-scooter Services (DES) at the same time.

[![uti](http://img.youtube.com/vi/Y2R46yeCXOQ/0.jpg)](https://youtu.be/Y2R46yeCXOQ)

First, we connect to the target Xiaomi ES3 e-scooter over BLE. The e-scooter name is in the standard Xiaomi format "MiScooter7723".
Second, we flash a malicious BMS firmware on the e-scooter. We explain how we develop it in our paper.
Third, we wait less than a minute for the update.
Fourth, we perform a BLE scan for nearby devices. The e-scooter name has changed to "3JA0016A1A00".
The first part "3JA0016A" is a unique fingerprint that allows to track the user via BLE scans.
Then, "1A" is the battery level (i.e., 26%) and "00" is the mileage, which is currently zero because we recently factory reset the device and did not drive it yet.
Fifth, we check the effectiveness of the DoS attack. The e-scooter is stuck on an infinite reboot loop, noticeable from the display and the constant noises (each beep is a reboot).
