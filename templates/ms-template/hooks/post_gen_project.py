# cat post_gen_project.py
import os
import shutil

print(os.getcwd())


def remove(filepath):
    if os.path.isfile(filepath):
        os.remove(filepath)
    elif os.path.isdir(filepath):
        shutil.rmtree(filepath)


with_db = '{{cookiecutter.with_db}}' == 'y'
service_package = '{{cookiecutter.service_package}}'

if not with_db:
    remove('src/main/resources/db')
    remove('src/main/java/com/mybank/'+service_package+'/domain/entity')
    remove('src/main/java/com/mybank/'+service_package+'/repository')
    remove('src/main/java/com/mybank/'+service_package+'/auditing')
    remove('src/main/java/com/mybank/'+service_package+'/config/PersistenceConfig.java')

