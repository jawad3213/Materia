import os
import re

src_root = r'c:\Users\elhai\OneDrive\Desktop\materia\backend\src\main\java\com\materia\backend'

# 1. Delete BaseWebRequest and BaseWebResponse
common_web = os.path.join(src_root, 'common', 'infrastructure', 'web')
base_req = os.path.join(common_web, 'BaseWebRequest.java')
base_res = os.path.join(common_web, 'BaseWebResponse.java')

if os.path.exists(base_req): os.remove(base_req)
if os.path.exists(base_res): os.remove(base_res)

# 2. Create BaseWebMapper
with open(os.path.join(common_web, 'BaseWebMapper.java'), 'w') as f:
    f.write('''package com.materia.backend.common.infrastructure.web;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Base interface for mapping between Web DTOs and Application Inputs/Outputs.
 *
 * @param <CWR> Create Web Request type
 * @param <UWR> Update Web Request type
 * @param <CI>  Create Application Input type
 * @param <UI>  Update Application Input type
 * @param <WR>  Web Response type
 * @param <O>   Application Output type
 */
public interface BaseWebMapper<CWR, UWR, CI, UI, WR, O> {

    CI toAppRequest(CWR webRequest);

    UI toAppRequest(UWR webRequest);

    WR toWebResponse(O appResponse);

    default List<WR> toWebResponseList(List<O> appResponses) {
        if (appResponses == null) {
            return List.of();
        }
        return appResponses.stream()
                .map(this::toWebResponse)
                .collect(Collectors.toList());
    }
}
''')

# 3. Remove "extends BaseWebRequest" and "extends BaseWebResponse" from DTOs
# Also remove their imports.
web_dtos_dir = os.path.join(src_root, 'contexts', 'masterdata', 'infrastructure', 'adapters', 'in', 'web', 'dtos')

for root, _, files in os.walk(web_dtos_dir):
    for file in files:
        if file.endswith('.java'):
            filepath = os.path.join(root, file)
            with open(filepath, 'r', encoding='utf-8') as f:
                content = f.read()
            
            # Remove imports
            content = re.sub(r'import com\.materia\.backend\.common\.infrastructure\.web\.BaseWebRequest;\n', '', content)
            content = re.sub(r'import com\.materia\.backend\.common\.infrastructure\.web\.BaseWebResponse;\n', '', content)
            
            # Remove extends
            content = re.sub(r' extends BaseWebRequest', '', content)
            content = re.sub(r' extends BaseWebResponse', '', content)
            
            with open(filepath, 'w', encoding='utf-8') as f:
                f.write(content)

# 4. Make the Web Mappers implement BaseWebMapper and remove the default method (since it's in interface)
mappers_dir = os.path.join(src_root, 'contexts', 'masterdata', 'infrastructure', 'adapters', 'in', 'web', 'mappers')

for file in os.listdir(mappers_dir):
    if file.endswith('WebMapper.java'):
        filepath = os.path.join(mappers_dir, file)
        module_capitalized = file.replace('WebMapper.java', '')
        
        with open(filepath, 'r', encoding='utf-8') as f:
            content = f.read()
        
        # Add import for BaseWebMapper
        if 'import com.materia.backend.common.infrastructure.web.BaseWebMapper;' not in content:
            content = content.replace('import org.springframework.stereotype.Component;', 
                                      'import com.materia.backend.common.infrastructure.web.BaseWebMapper;\nimport org.springframework.stereotype.Component;')
            
        # Add implements clause
        CWR = f'Create{module_capitalized}WebRequest'
        UWR = f'Update{module_capitalized}WebRequest'
        CI = f'Create{module_capitalized}Input'
        UI = f'Update{module_capitalized}Input'
        WR = f'{module_capitalized}WebResponse'
        O = f'{module_capitalized}Output'
        
        interface_str = f'implements BaseWebMapper<{CWR}, {UWR}, {CI}, {UI}, {WR}, {O}>'
        
        content = re.sub(f'public class {module_capitalized}WebMapper \\{{', f'public class {module_capitalized}WebMapper {interface_str} {{', content)
        
        # Add @Override annotations
        content = re.sub(r'public ' + CI + r' toAppRequest\(' + CWR, '@Override\n    public ' + CI + ' toAppRequest(' + CWR, content)
        content = re.sub(r'public ' + UI + r' toAppRequest\(' + UWR, '@Override\n    public ' + UI + ' toAppRequest(' + UWR, content)
        content = re.sub(r'public ' + WR + r' toWebResponse\(' + O, '@Override\n    public ' + WR + ' toWebResponse(' + O, content)
        
        # Remove the List method since it's a default method in the interface now
        # We find public List<WR> toWebResponseList(List<O> appResponses) { ... }
        # Let's just use regex to remove the method completely
        pattern = r'public List<' + WR + r'> toWebResponseList\(List<' + O + r'> appResponses\).*?\}'
        content = re.sub(pattern, '', content, flags=re.DOTALL)
        
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(content)

print("Web DTO cleanup and BaseWebMapper integration completed.")
