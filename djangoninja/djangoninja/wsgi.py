"""
WSGI config for djangoninja project.

It exposes the WSGI callable as a module-level variable named ``application``.

For more information on this file, see
https://docs.djangoproject.com/en/5.1/howto/deployment/wsgi/
"""

import os

from django.core.wsgi import get_wsgi_application

from djangoninja.repository.post_repository import PostRepository

os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'djangoninja.settings')

application = get_wsgi_application()


PostRepository.ensure_post_table_exists()
