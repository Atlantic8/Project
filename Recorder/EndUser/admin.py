from django.contrib import admin

# Register your models here.

from EndUser.models import EndUser, Store, Customer
import sys


reload(sys)
sys.setdefaultencoding('utf8')

'''
class EndUserAdmin(admin.ModelAdmin):
    fieldsets = [
        'username', 'password', 'create_time',
        'email', 'permission', 'store_name',
        'discount', 'cart', 'contact'
    ]
'''

admin.site.register(EndUser)
admin.site.register(Store)
admin.site.register(Customer)