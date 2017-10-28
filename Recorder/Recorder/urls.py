"""Recorder URL Configuration

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
from EndUser import views as user_view
from Record import views as record_view
from django.conf import  settings
from django.conf.urls.static import static


urlpatterns = [
    url(r'^admin/', admin.site.urls),
    url(r'^logout/', user_view.do_logout),
    url(r'^login/', user_view.do_login),
]

urlpatterns += static(settings.MEDIA_URL, document_root=settings.MEDIA_ROOT)

urlpatterns += [
    url(r'^records/detail/(.+)', record_view.get_single_record),
    url(r'^records/cart/buy/', record_view.buy_cart),
    url(r'^records/cart/remove/(.+)/', record_view.remove_from_cart), # remove item_id
    url(r'^records/cart/add/', record_view.add_to_cart), # add item_id
    url(r'^records/cart/', record_view.get_cart),
    url(r'^records/sales_record/(\d+)', record_view.get_sales_record),
    url(r'^records/(\d+)', record_view.get_record),
    url(r'^records/filter/(\d+)', record_view.get_record),
    url(r'^account/', record_view.get_account),
    url(r'^reset_password/', record_view.reset_password),
    url(r'^reset_announcement/', record_view.reset_announcement),
]

urlpatterns += [
    url(r'^', record_view.main_site),
]