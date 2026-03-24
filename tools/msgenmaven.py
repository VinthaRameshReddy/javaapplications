#!/usr/bin/env python3

from cookiecutter.main import cookiecutter
import random
import os
import sys
import getopt

def generate_microservice(output_dir):
    print("generating Microservice in directory: ", os.path.abspath(output_dir))

    service_port = random.randint(10000, 15000)
    db_port = random.randint(15000, 20000)

    cookiecutter(
        '../templates/msmaven-template',
        extra_context={'service_port': service_port,
                    'output_dir': output_dir, 'db_port': db_port},
        output_dir=output_dir,
    )

def generate_lib(output_dir):
    print("generating lib in directory: ", os.path.abspath(output_dir))

    cookiecutter(
        '../templates/lib-template',
        output_dir=output_dir,
    ) 

try:
    arguments, values = getopt.getopt(sys.argv[1:], "hls:", ["help", "lib","service"])
    if len(arguments) == 0:
        print("To use the tool run the following command..")
        print('./msgenmaven.py --service')
        print('or')
        print('./msgenmaven.py --lib')
        sys.exit()
except getopt.GetoptError:
    print('./msgenmaven.py -o <output_directory>')
    sys.exit(2)
for opt, arg in arguments:
    if opt in ("-h", "--help"):
        print("To use the tool run the following command..")
        print('./msgenmaven.py --service')
        print('or')
        print('./msgenmaven.py --lib')
        sys.exit()
    elif opt in ("-l", "--lib") :
        output_dir = "../shared"
        generate_lib(output_dir)
    elif opt in ("-s", "--service") :
        output_dir = "../services"
        generate_microservice(output_dir)
    else:
        print("To use the tool run the following command..")
        print('./msgenmaven.py --service')
        print('or')
        print('./msgenmaven.py --lib')
        sys.exit()




