import binascii

def leggi_file_binario(file_path):
    with open(file_path, 'rb') as file:
        return file.read()

def confronta_file_binari(file1, file2):
    len_file1 = len(file1)
    len_file2 = len(file2)
    len_min = min(len_file1, len_file2)

    differenze = []

    for i in range(len_min):
        byte1 = file1[i]
        byte2 = file2[i]
        if byte1 != byte2:
            differenze.append((i, byte1, byte2))

    if len_file1 > len_file2:
        for i in range(len_min, len_file1):
            differenze.append((i, file1[i], None))
    elif len_file2 > len_file1:
        for i in range(len_min, len_file2):
            differenze.append((i, None, file2[i]))

    return differenze

def mostra_differenze(file_path1, file_path2):
    file1 = leggi_file_binario(file_path1)
    file2 = leggi_file_binario(file_path2)

    differenze = confronta_file_binari(file1, file2)

    if differenze:
        print("Differenze trovate:")
        for posizione, byte1, byte2 in differenze:
            if byte1 is not None and byte2 is not None:
                print(f"All'indirizzo {hex(posizione)} c'è una differenza: {hex(byte1)} -> {hex(byte2)}")
            elif byte1 is not None:
                print(f"Il primo file ha un byte aggiuntivo all'indirizzo {hex(posizione)}: {hex(byte1)}")
            else:
                print(f"Il secondo file ha un byte aggiuntivo all'indirizzo {hex(posizione)}: {hex(byte2)}")
    else:
        print("I file sono identici")



# Inserisci i nomi dei file binari da confrontare
nome_file1 = "1.4.1.bin"
nome_file2 = "BMS141_mi3.pro2.1s.essential_batterySpoofing.bin"

mostra_differenze(nome_file1, nome_file2)
