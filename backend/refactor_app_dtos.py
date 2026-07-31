import os
import shutil
import re

src_root = r'c:\Users\elhai\OneDrive\Desktop\materia\backend\src\main\java\com\materia\backend'

# File renames
renames = {
    os.path.join(src_root, 'common', 'application', 'BaseRequest.java'): 'BaseInput.java',
    os.path.join(src_root, 'common', 'application', 'BaseResponse.java'): 'BaseOutput.java',
    os.path.join(src_root, 'contexts', 'masterdata', 'application', 'dtos', 'category', 'CreateCategoryRequest.java'): 'CreateCategoryInput.java',
    os.path.join(src_root, 'contexts', 'masterdata', 'application', 'dtos', 'category', 'UpdateCategoryRequest.java'): 'UpdateCategoryInput.java',
    os.path.join(src_root, 'contexts', 'masterdata', 'application', 'dtos', 'category', 'CategoryResponseDto.java'): 'CategoryOutput.java',
    os.path.join(src_root, 'contexts', 'masterdata', 'application', 'dtos', 'material', 'CreateMaterialRequest.java'): 'CreateMaterialInput.java',
    os.path.join(src_root, 'contexts', 'masterdata', 'application', 'dtos', 'material', 'UpdateMaterialRequest.java'): 'UpdateMaterialInput.java',
    os.path.join(src_root, 'contexts', 'masterdata', 'application', 'dtos', 'material', 'MaterialResponseDto.java'): 'MaterialOutput.java',
    os.path.join(src_root, 'contexts', 'masterdata', 'application', 'dtos', 'supplier', 'CreateSupplierRequest.java'): 'CreateSupplierInput.java',
    os.path.join(src_root, 'contexts', 'masterdata', 'application', 'dtos', 'supplier', 'UpdateSupplierRequest.java'): 'UpdateSupplierInput.java',
    os.path.join(src_root, 'contexts', 'masterdata', 'application', 'dtos', 'supplier', 'SupplierResponseDto.java'): 'SupplierOutput.java',
}

for old_path, new_name in renames.items():
    if os.path.exists(old_path):
        new_path = os.path.join(os.path.dirname(old_path), new_name)
        shutil.move(old_path, new_path)
        print(f"Renamed {os.path.basename(old_path)} to {new_name}")

# Content replacements mapping (Regex \b \b style to avoid partial matches)
replacements = {
    r'\bBaseRequest\b': 'BaseInput',
    r'\bBaseResponse\b': 'BaseOutput',
    r'\bCreateCategoryRequest\b': 'CreateCategoryInput',
    r'\bUpdateCategoryRequest\b': 'UpdateCategoryInput',
    r'\bCategoryResponseDto\b': 'CategoryOutput',
    r'\bCreateMaterialRequest\b': 'CreateMaterialInput',
    r'\bUpdateMaterialRequest\b': 'UpdateMaterialInput',
    r'\bMaterialResponseDto\b': 'MaterialOutput',
    r'\bCreateSupplierRequest\b': 'CreateSupplierInput',
    r'\bUpdateSupplierRequest\b': 'UpdateSupplierInput',
    r'\bSupplierResponseDto\b': 'SupplierOutput'
}

def update_file_contents(directory):
    for root, _, files in os.walk(directory):
        for file in files:
            if file.endswith('.java'):
                filepath = os.path.join(root, file)
                with open(filepath, 'r', encoding='utf-8') as f:
                    content = f.read()
                
                original_content = content
                for pattern, replacement in replacements.items():
                    content = re.sub(pattern, replacement, content)
                
                if content != original_content:
                    with open(filepath, 'w', encoding='utf-8') as f:
                        f.write(content)

update_file_contents(src_root)
print("File contents updated successfully.")
