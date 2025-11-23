import os
import shutil
import subprocess
from pathlib import Path

# def create_maven_archetype():
#     """Create custom Maven archetype for API"""
#     print("Creating Maven archetype...")
    
#     archetype_dir = Path("api/maven-archetype")
#     if not archetype_dir.exists():
#         return
    
#     # Maven archetype structure is already in template
#     print("Maven archetype structure created")

def create_angular_project():
    """Create Angular project using ng new"""
    print("\n" + "="*60)
    print("Creating Angular project with Angular CLI...")
    print("="*60 + "\n")
    
    frontend_dir = Path("frontend")
    
    # Check if Angular CLI is available
    try:
        result = subprocess.run(
            ["ng", "version"], 
            check=True, 
            capture_output=True,
            text=True
        )
        print(f"Angular CLI found: {result.stdout.split()[0]}")
    except (subprocess.CalledProcessError, FileNotFoundError):
        print("ERROR: Angular CLI not found!")
        print("\nPlease install Angular CLI first:")
        print("  npm install -g @angular/cli")
        print("\nThen manually create the Angular project:")
        print(f"  cd {frontend_dir}")
        print("  ng new {{ cookiecutter.project.name }}-frontend --directory ./frontend --routing --style=scss --skip-git")
        return False
    
    # Create Angular project
    print(f"Running 'ng new' in {frontend_dir}...")
    print("This may take a few minutes...\n")
    
    try:
        # Change to frontend directory
        original_dir = os.getcwd()
        os.chdir(frontend_dir)
        
        # Run ng new with specific options
        result = subprocess.run([
            "ng", "new", 
            "{{ cookiecutter.project.name }}",
            "--directory", "./frontend",
            "--routing",
            "--style", "scss",
            "--skip-git",
            "--skip-install"  # Skip npm install for now
        ], 
        check=True,
        text=True,
        input="y\n"  # Auto-answer yes to prompts
        )
        
        print("\n✓ Angular project structure created successfully!")
        
        # Now run npm install
        print("\nInstalling npm dependencies...")
        subprocess.run(["npm", "install"], check=True)
        print("✓ Dependencies installed successfully!")
        
        # Return to original directory
        os.chdir(original_dir)
        return True
        
    except subprocess.CalledProcessError as e:
        print(f"\n✗ Error creating Angular project: {e}")
        os.chdir(original_dir)
        return False
    except Exception as e:
        print(f"\n✗ Unexpected error: {e}")
        os.chdir(original_dir)
        return False

# def add_security_code():
#     """Add security-related code if selected"""
#     if '{{ cookiecutter.include_security }}' == 'yes':
#         print("Adding security components...")
#         # Security files are already in template structure

# def add_angular_features():
#     """Add Angular features based on selections"""
#     features = []
    
#     if '{{ cookiecutter.include_authentication_guards }}' == 'yes':
#         features.append("authentication guards")
    
#     if '{{ cookiecutter.include_http_interceptors }}' == 'yes':
#         features.append("HTTP interceptors")
    
#     if features:
#         print(f"Adding Angular features: {', '.join(features)}")

# def remove_unused_files():
#     """Remove files for features not selected"""
    
#     if '{{ cookiecutter.include_security }}' == 'no':
#         security_files = [
#             'api/maven-archetype/archetype-resources/src/main/java/security',
#             'api/maven-archetype/archetype-resources/src/main/java/service/UserService.java'
#         ]
#         for file_path in security_files:
#             path = Path(file_path)
#             if path.exists():
#                 if path.is_dir():
#                     shutil.rmtree(path)
#                 else:
#                     path.unlink()
    
#     if '{{ cookiecutter.include_authentication_guards }}' == 'no':
#         Path('frontend/src/app/guards').rmdir() if Path('frontend/src/app/guards').exists() else None
    
#     if '{{ cookiecutter.include_http_interceptors }}' == 'no':
#         Path('frontend/src/app/interceptors').rmdir() if Path('frontend/src/app/interceptors').exists() else None

# def create_env_file():
#     """Create .env from .env.example"""
#     shutil.copy('.env.example', '.env')
#     print("Created .env file from .env.example")

def main():
    print("\n" + "="*60)
    print("Setting up project: {{ cookiecutter.project.name }}")
    print("="*60 + "\n")
    
    # create_env_file()
    # create_maven_archetype()
    # remove_unused_files()
    # add_security_code()
    
    # Create Angular project
    angular_success = create_angular_project()
    
    # if angular_success:
        # add_angular_features()
    
    print("\n" + "="*60)
    print("Project created successfully!")
    print("="*60)
    print("\nNext steps:")
    print("1. cd {{ cookiecutter.project.name }}")
    print("2. Review and update .env file")
    
    if not angular_success:
        print("3. Create Angular project: cd frontend && ng new {{ cookiecutter.project.name }}-frontend --directory ./ --routing --style=scss")
        print("4. Create Maven project: cd api && mvn archetype:generate -B ...")
        print("5. Start services: docker-compose up -d")
    else:
        print("3. Create Maven project: cd api && mvn archetype:generate -B ...")
        print("4. Start services: docker-compose up -d")
    
    print("\nFor more details, see README.md")

if __name__ == '__main__':
    main()