import os
import re

src_root = r'c:\Users\elhai\OneDrive\Desktop\materia\backend\src\main\java\com\materia\backend'

web_layer = os.path.join(src_root, 'contexts', 'masterdata', 'infrastructure', 'adapters', 'in', 'web')
mappers_dir = os.path.join(web_layer, 'mappers')
controllers_dir = os.path.join(web_layer, 'controllers')

# 1. Update Mappers
for root, _, files in os.walk(mappers_dir):
    for file in files:
        if file.endswith('WebMapper.java'):
            filepath = os.path.join(root, file)
            with open(filepath, 'r', encoding='utf-8') as f:
                content = f.read()
            
            module = file.replace('WebMapper.java', '')
            
            content = re.sub(r'toAppRequest\(Create' + module + r'WebRequest', 'toAppCreateRequest(Create' + module + 'WebRequest', content)
            content = re.sub(r'toAppRequest\(Update' + module + r'WebRequest', 'toAppUpdateRequest(Update' + module + 'WebRequest', content)
            
            with open(filepath, 'w', encoding='utf-8') as f:
                f.write(content)

# 2. Update Controllers
for root, _, files in os.walk(controllers_dir):
    for file in files:
        if file.endswith('Controller.java'):
            filepath = os.path.join(root, file)
            with open(filepath, 'r', encoding='utf-8') as f:
                content = f.read()
            
            lines = content.split('\n')
            for i, line in enumerate(lines):
                if '.toAppRequest(' in line:
                    is_update = False
                    # Look backwards to find if it's create or update
                    for j in range(i, max(-1, i-15), -1):
                        if 'Update' in lines[j] and 'Mapping' in lines[j]:
                            is_update = True
                            break
                        if 'Create' in lines[j] and 'Mapping' in lines[j]:
                            break
                        if 'Update' in lines[j] and 'WebRequest' in lines[j]:
                            is_update = True
                            break
                    
                    if is_update:
                        lines[i] = lines[i].replace('.toAppRequest(', '.toAppUpdateRequest(')
                    else:
                        lines[i] = lines[i].replace('.toAppRequest(', '.toAppCreateRequest(')

            content = '\n'.join(lines)
            
            with open(filepath, 'w', encoding='utf-8') as f:
                f.write(content)

print("Mappers and Controllers updated.")
