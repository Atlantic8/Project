from django.contrib import admin

# Register your models here.
from Record.models import product,sales_record

import sys;

reload(sys);
sys.setdefaultencoding("utf8")

admin.site.register(product)
admin.site.register(sales_record)