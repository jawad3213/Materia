import os
import shutil

src_root = r'c:\Users\elhai\OneDrive\Desktop\materia\backend\src\main\java\com\materia\backend'

# The two DTO base directories
app_dtos_dir = os.path.join(src_root, 'contexts', 'masterdata', 'application', 'dtos')
web_dtos_dir = os.path.join(src_root, 'contexts', 'masterdata', 'infrastructure', 'adapters', 'in', 'web', 'dtos')

modules = ['category', 'material', 'supplier']

def flatten_dto_folder(base_dto_dir, module_name):
    module_dir = os.path.join(base_dto_dir, module_name)
    if not os.path.exists(module_dir):
        return

    req_dir = os.path.join(module_dir, 'requests')
    res_dir = os.path.join(module_dir, 'responses')

    # Move files from requests
    if os.path.exists(req_dir):
        for file in os.listdir(req_dir):
            if file.endswith('.java'):
                src_file = os.path.join(req_dir, file)
                dst_file = os.path.join(module_dir, file)
                shutil.move(src_file, dst_file)
                # Update package declaration
                with open(dst_file, 'r', encoding='utf-8') as f:
                    content = f.read()
                content = content.replace('.requests;', ';')
                with open(dst_file, 'w', encoding='utf-8') as f:
                    f.write(content)
        os.rmdir(req_dir)

    # Move files from responses
    if os.path.exists(res_dir):
        for file in os.listdir(res_dir):
            if file.endswith('.java'):
                src_file = os.path.join(res_dir, file)
                dst_file = os.path.join(module_dir, file)
                shutil.move(src_file, dst_file)
                # Update package declaration
                with open(dst_file, 'r', encoding='utf-8') as f:
                    content = f.read()
                content = content.replace('.responses;', ';')
                with open(dst_file, 'w', encoding='utf-8') as f:
                    f.write(content)
        os.rmdir(res_dir)

# Flatten application and web DTO folders
for mod in modules:
    flatten_dto_folder(app_dtos_dir, mod)
    flatten_dto_folder(web_dtos_dir, mod)

# Now, update all imports in the entire project
def update_imports(root_dir):
    for root, dirs, files in os.walk(root_dir):
        for file in files:
            if file.endswith('.java'):
                file_path = os.path.join(root, file)
                with open(file_path, 'r', encoding='utf-8') as f:
                    content = f.read()
                
                original_content = content
                
                # Replace imports
                content = content.replace('.dtos.category.requests.', '.dtos.category.')
                content = content.replace('.dtos.category.responses.', '.dtos.category.')
                content = content.replace('.dtos.material.requests.', '.dtos.material.')
                content = content.replace('.dtos.material.responses.', '.dtos.material.')
                content = content.replace('.dtos.supplier.requests.', '.dtos.supplier.')
                content = content.replace('.dtos.supplier.responses.', '.dtos.supplier.')
                
                if content != original_content:
                    with open(file_path, 'w', encoding='utf-8') as f:
                        f.write(content)

update_imports(src_root)
print("DTO folders flattened and imports updated successfully.")
