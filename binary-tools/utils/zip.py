import zipfile

def create_zip_with_files(zip_file_name):

    with zipfile.ZipFile(zip_file_name, 'w') as zip_file:
        zip_file.write('info.json')
        zip_file.write('FIRM.bin')

    print(f"File '{zip_file_name}' creato con successo.")
