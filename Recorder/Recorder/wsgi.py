"""
WSGI config for Recorder project.

It exposes the WSGI callable as a module-level variable named ``application``.

For more information on this file, see
https://docs.djangoproject.com/en/1.10/howto/deployment/wsgi/
"""

import os,sys

from django.core.wsgi import get_wsgi_application
from os.path import abspath, dirname

project_dir = dirname(dirname(abspath(__file__)))

#os.path.append("/home/atlantic8/code/Recorder/Recorder")
sys.path.append("/var/www/Recorder/")
sys.path.insert(0, project_dir)

os.environ.setdefault("DJANGO_SETTINGS_MODULE", "Recorder.settings")

application = get_wsgi_application()
