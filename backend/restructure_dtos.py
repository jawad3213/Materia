import os
import shutil

base_app_dir = r'c:\Users\elhai\OneDrive\Desktop\materia\backend\src\main\java\com\materia\backend\contexts\masterdata\application'
dtos_dir = os.path.join(base_app_dir, 'dtos')
entities = ['category', 'material', 'supplier']

# Create new directory structure
for folder in ['models', 'requests', 'responses']:
    for entity in entities:
        os.makedirs(os.path.join(dtos_dir, folder, entity), exist_ok=True)

for entity in entities:
    old_dir = os.path.join(dtos_dir, entity)
    if not os.path.exists(old_dir):
        continue
        
    for filename in os.listdir(old_dir):
        if not filename.endswith('.java'):
            continue
            
        old_path = os.path.join(old_dir, filename)
        
        # Determine new folder
        if filename.endswith('Request.java'):
            new_folder = 'requests'
        elif filename.endswith('Response.java'):
            new_folder = 'responses'
        else:
            new_folder = 'models'
            
        new_path = os.path.join(dtos_dir, new_folder, entity, filename)
        
        # Read content, update package, write to new path
        with open(old_path, 'r') as f:
            content = f.read()
            
        old_package = f'package com.materia.backend.contexts.masterdata.application.dtos.{entity};'
        new_package = f'package com.materia.backend.contexts.masterdata.application.dtos.{new_folder}.{entity};'
        
        content = content.replace(old_package, new_package)
        
        # If it's a Request or Response, it probably uses the Model (Dto), so we need to import it
        if new_folder in ['requests', 'responses']:
            import_statement = f'import com.materia.backend.contexts.masterdata.application.dtos.models.{entity}.*;'
            # Insert the import after the package declaration
            content = content.replace(new_package, f'{new_package}\n\n{import_statement}')
            
        with open(new_path, 'w') as f:
            f.write(content)
            
        # Delete old file
        os.remove(old_path)
        
    # Delete old entity directory if empty
    try:
        os.rmdir(old_dir)
    except OSError:
        pass

# Update Mappers and UseCases
import_replacements = {}
for entity in entities:
    old_import = f'import com.materia.backend.contexts.masterdata.application.dtos.{entity}.*;'
    new_imports = f'''import com.materia.backend.contexts.masterdata.application.dtos.models.{entity}.*;
import com.materia.backend.contexts.masterdata.application.dtos.requests.{entity}.*;
import com.materia.backend.contexts.masterdata.application.dtos.responses.{entity}.*;'''
    import_replacements[old_import] = new_imports

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

print('DTOs restructured and imports updated successfully.')
