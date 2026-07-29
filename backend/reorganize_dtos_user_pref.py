import os
import shutil

base_app_dir = r'c:\Users\elhai\OneDrive\Desktop\materia\backend\src\main\java\com\materia\backend\contexts\masterdata\application'
dtos_dir = os.path.join(base_app_dir, 'dtos')
entities = ['category', 'material', 'supplier']

# Create new directory structure
for entity in entities:
    os.makedirs(os.path.join(dtos_dir, entity, 'requests'), exist_ok=True)
    os.makedirs(os.path.join(dtos_dir, entity, 'responses'), exist_ok=True)

for entity in entities:
    # 1. Move from models/{entity} -> {entity}/
    models_dir = os.path.join(dtos_dir, 'models', entity)
    if os.path.exists(models_dir):
        for filename in os.listdir(models_dir):
            if filename.endswith('.java'):
                old_path = os.path.join(models_dir, filename)
                new_path = os.path.join(dtos_dir, entity, filename)
                
                with open(old_path, 'r') as f:
                    content = f.read()
                
                old_package = f'package com.materia.backend.contexts.masterdata.application.dtos.models.{entity};'
                new_package = f'package com.materia.backend.contexts.masterdata.application.dtos.{entity};'
                content = content.replace(old_package, new_package)
                
                with open(new_path, 'w') as f:
                    f.write(content)
                os.remove(old_path)
                
    # 2. Move from requests/{entity} -> {entity}/requests/
    requests_dir = os.path.join(dtos_dir, 'requests', entity)
    if os.path.exists(requests_dir):
        for filename in os.listdir(requests_dir):
            if filename.endswith('.java'):
                old_path = os.path.join(requests_dir, filename)
                new_path = os.path.join(dtos_dir, entity, 'requests', filename)
                
                with open(old_path, 'r') as f:
                    content = f.read()
                
                old_package = f'package com.materia.backend.contexts.masterdata.application.dtos.requests.{entity};'
                new_package = f'package com.materia.backend.contexts.masterdata.application.dtos.{entity}.requests;'
                content = content.replace(old_package, new_package)
                
                old_import = f'import com.materia.backend.contexts.masterdata.application.dtos.models.{entity}.*;'
                new_import = f'import com.materia.backend.contexts.masterdata.application.dtos.{entity}.*;'
                content = content.replace(old_import, new_import)
                
                with open(new_path, 'w') as f:
                    f.write(content)
                os.remove(old_path)

    # 3. Move from responses/{entity} -> {entity}/responses/
    responses_dir = os.path.join(dtos_dir, 'responses', entity)
    if os.path.exists(responses_dir):
        for filename in os.listdir(responses_dir):
            if filename.endswith('.java'):
                old_path = os.path.join(responses_dir, filename)
                new_path = os.path.join(dtos_dir, entity, 'responses', filename)
                
                with open(old_path, 'r') as f:
                    content = f.read()
                
                old_package = f'package com.materia.backend.contexts.masterdata.application.dtos.responses.{entity};'
                new_package = f'package com.materia.backend.contexts.masterdata.application.dtos.{entity}.responses;'
                content = content.replace(old_package, new_package)
                
                old_import = f'import com.materia.backend.contexts.masterdata.application.dtos.models.{entity}.*;'
                new_import = f'import com.materia.backend.contexts.masterdata.application.dtos.{entity}.*;'
                content = content.replace(old_import, new_import)
                
                with open(new_path, 'w') as f:
                    f.write(content)
                os.remove(old_path)

# Delete old root folders
for folder in ['models', 'requests', 'responses']:
    try:
        shutil.rmtree(os.path.join(dtos_dir, folder))
    except OSError:
        pass

# Update Mappers and UseCases
import_replacements = {}
for entity in entities:
    old_imports = f'''import com.materia.backend.contexts.masterdata.application.dtos.models.{entity}.*;
import com.materia.backend.contexts.masterdata.application.dtos.requests.{entity}.*;
import com.materia.backend.contexts.masterdata.application.dtos.responses.{entity}.*;'''
    
    new_imports = f'''import com.materia.backend.contexts.masterdata.application.dtos.{entity}.*;
import com.materia.backend.contexts.masterdata.application.dtos.{entity}.requests.*;
import com.materia.backend.contexts.masterdata.application.dtos.{entity}.responses.*;'''
    import_replacements[old_imports] = new_imports

def update_imports(directory):
    for root, _, files in os.walk(directory):
        for filename in files:
            if filename.endswith('.java'):
                filepath = os.path.join(root, filename)
                with open(filepath, 'r') as f:
                    content = f.read()
                
                changed = False
                for old_imp, new_imps in import_replacements.items():
                    if old_imp in content:
                        content = content.replace(old_imp, new_imps)
                        changed = True
                        
                if changed:
                    with open(filepath, 'w') as f:
                        f.write(content)

update_imports(os.path.join(base_app_dir, 'mappers'))
update_imports(os.path.join(base_app_dir, 'usecases'))

print('DTOs re-restructured according to user preference successfully.')
