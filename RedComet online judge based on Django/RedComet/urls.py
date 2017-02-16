"""RedComet URL Configuration

The `urlpatterns` list routes URLs to views. For more information please see:
    https://docs.djangoproject.com/en/1.10/topics/http/urls/
Examples:
Function views
    1. Add an import:  from my_app import views
    2. Add a URL to urlpatterns:  url(r'^$', views.home, name='home')
Class-based views
    1. Add an import:  from other_app.views import Home
    2. Add a URL to urlpatterns:  url(r'^$', Home.as_view(), name='home')
Including another URLconf
    1. Import the include() function: from django.conf.urls import url, include
    2. Add a URL to urlpatterns:  url(r'^blog/', include('blog.urls'))
"""
from django.conf.urls import url
from django.contrib import admin
from django.contrib.staticfiles.urls import staticfiles_urlpatterns
from account import views as account_view
from problem import views as problem_view
from submission import views as submission_view
from django.conf import settings

urlpatterns = [
    url(r'^admin/', admin.site.urls),
    # url(r'^static/(?P<path>.*)$','django.views.static.serve',{'document_root':settings.STATIC_ROOT}, name='static'),
]

#urlpatterns += staticfiles_urlpatterns()

# account-related
urlpatterns += [
    url(r'^accounts/login/$', account_view.login),
    url(r'^accounts/register/$', account_view.register),
    url(r'^accounts/logout/$', account_view.logout),
]

# problem-related
urlpatterns += [
    url(r'^problemset/$', problem_view.problemset),
    # problemset/nim
    url(r'^problemset/problem/(.{3,50})/$',problem_view.problem_page),
    url(r'^problemset/page/(\d+)/$',problem_view.problemset),
    url(r'^problemset/submit/$',problem_view.submit),
]

# submission-related
urlpatterns += [
    # submission/page/2 to present submissions in pages
    url(r'^submission/page/(\d+)/$', submission_view.all_submission),
    # submission/nim/page1 to present submissions of a certain problem in pages
    url(r'^submission/.{3,50}/page/(\d+)/$', submission_view.submission_problem),
    # present one submission
    url(r'^submission/detail/$', submission_view.submission_page),
]
