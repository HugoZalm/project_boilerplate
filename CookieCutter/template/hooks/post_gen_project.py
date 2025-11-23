import os
import shutil
import subprocess
from pathlib import Path

def create_angular_project():
    """Create Angular project using ng new"""
    print("\n" + "="*60)
    print("Creating Angular project with Angular CLI...")
    print("="*60 + "\n")
    
    frontend_dir = Path("frontend")
    
    # Check if Angular CLI is available
    try:
        result = subprocess.run(
            ["ng", "--help"], 
            check=True, 
            capture_output=True,
            text=True
        )
        # Try to get version info (won't fail if outside project)
        version_result = subprocess.run(
            ["ng", "version"],
            capture_output=True,
            text=True
            # Don't check=True here, as it may fail outside Angular project
        )
        print(f"✓ Angular CLI found")
        if "Angular CLI:" in version_result.stdout:
            # Extract CLI version if available
            for line in version_result.stdout.split('\n'):
                if 'Angular CLI:' in line:
                    print(f"  {line.strip()}")
                    break
    except FileNotFoundError:
        print("✗ ERROR: Angular CLI not found!")
        print("\nPlease install Angular CLI first:")
        print("  npm install -g @angular/cli")
        print("\nThen manually create the Angular project:")
        print(f"  cd {frontend_dir}")
        print("  ng new {{ cookiecutter.project.name }}-frontend --directory ./ --routing --style=scss --skip-git")
        return False
    except subprocess.CalledProcessError:
        print("✗ ERROR: Angular CLI not found or not working properly!")
        print("\nPlease install Angular CLI first:")
        print("  npm install -g @angular/cli")
        return False
    
def add_angular_features():
    """Add Angular features based on selections"""
    features = []

def main():
    print("\n" + "="*60)
    print("Setting up project: {{ cookiecutter.project.name }}")
    print("="*60 + "\n")
    
    # Create Angular project
    angular_success = create_angular_project()
    
    if angular_success:
        add_angular_features()

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
