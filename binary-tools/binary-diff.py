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
        print("Differences found:")
        for posizione, byte1, byte2 in differenze:
            if byte1 is not None and byte2 is not None:
                print(f"At address {hex(posizione)} there a difference: {hex(byte1)} -> {hex(byte2)}")
            elif byte1 is not None:
                print(f"First file has an additional byte at address {hex(posizione)}: {hex(byte1)}")
            else:
                print(f"Second file has an additional byte at address {hex(posizione)}: {hex(byte2)}")
    else:
        print("Identical files")



# Change to the BCTRL bin files you want to analyze
bin1 = "1.4.1.bin"
bin2 = "..\\malicious-payloads\\ES3\\uti.bin"

mostra_differenze(bin1, bin2)
