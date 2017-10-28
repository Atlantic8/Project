# -*- coding: utf-8 -*-
from __future__ import unicode_literals
from django.db import models
from tool.storage import ImageStorage
from EndUser.models import Customer, Store
import time, random
import django.utils.timezone as timezone

# Create your models here.

def decode(info):
      return info.decode('utf-8')

def default_product_id():
    return str(int(time.time()))+str(random.randint(1, 10000000))

class product(models.Model):
    item_id = models.CharField(verbose_name='商品编号', max_length=50, primary_key=True, unique=True, default=default_product_id)

    image = models.ImageField(verbose_name='图片', upload_to='image/')            # **    ok

    serial_no = models.CharField(verbose_name='序列号', max_length=100)                  # ** ok

    description = models.CharField(verbose_name='商品描述', max_length=1000, blank=True, null=True)   # ** ok

    report_no = models.CharField(verbose_name='报告序号', max_length=50, blank=True, null=True)       # ** ok

    category = models.CharField(verbose_name='种类', max_length=50, blank=True, null=True)        #    ok

    pieces = models.CharField(verbose_name='块数', max_length=20, blank=True, null=True)         #     ok

    size = models.CharField(verbose_name='大小', max_length=20, blank=True, null=True)             # **  ok

    weight = models.CharField(verbose_name='重量', max_length=20, blank=True, null=True)  # **   ok

    main_material_name = models.CharField(verbose_name='主要材料', max_length=50, blank=True, null=True)   # **  ok

    main_stone_name = models.CharField(verbose_name='主要石料', max_length=50,blank=True, null=True) # **  ok

    main_stone_weight = models.CharField(verbose_name='石料重量', max_length=20,blank=True, null=True) # **  ok

    general_information = models.CharField(verbose_name='大致信息', max_length=1000,blank=True, null=True) # **  ok
    # salesman cannot refer to this
    supplier = models.CharField(verbose_name='供应商', max_length=20,blank=True, null=True)

    comment = models.CharField(verbose_name='评论', max_length=1000, blank=True, null=True)          #        ok
    # salesman cannot refer to this
    net_price = models.CharField(verbose_name='网上价格', max_length=20, blank=True, null=True)

    sales_price = models.CharField(verbose_name='销售价格', max_length=20, blank=True, null=True)      # **

    # discount, default to be 100%
    discount = models.CharField(verbose_name='折扣', max_length=4, default='100')

    # in which store you can buy this one
    store_name = models.CharField(verbose_name='商店名称', max_length=20, default='#')

    # is wholesale or not
    wholesale = models.BooleanField(verbose_name='是否批发', default=False)

    # 1 for sold, 0: not sold
    is_sold = models.BooleanField(verbose_name='是否售出', default=False)
    
    '''
    trade_date = models.DateTimeField(auto_now_add=True, null=True) # 

    trade_id = models.ForeignKey(sales_record, default='#', max_length=10)

    #item_id = models.CharField(max_length=50, blank=True, null=True)
    
    trade_price = models.CharField(max_length=20, blank=True, null=True)

    received_date  = models.DateTimeField(auto_now_add=True, null=True)

    customer_id = models.CharField(max_length=50, blank=True, null=True)

    sales_personnel = models.CharField(max_length=30,blank=True, null=True)   # 

    location = models.CharField(max_length=500, blank=True, null=True)       # 

    '''
    def __str__(self):
    	return self.serial_no+' : '+self.category

    class Meta:
        verbose_name = '商品'

        verbose_name_plural = '商品'


# sales records
class sales_record(models.Model):
    
    trade_id = models.CharField(verbose_name='交易编号', unique=True, primary_key=True, max_length=30)

    trade_date = models.DateTimeField('交易日期', default=timezone.now) # **

    item_id = models.CharField(verbose_name='商品编号', max_length=50, blank=True, null=True)

    discount = models.CharField(verbose_name='折扣', max_length=10, default='100')

    trade_price = models.CharField(verbose_name='交易价格', max_length=20, blank=True, null=True)

    received_date  = models.DateTimeField(verbose_name='收货日期', auto_now_add=True, null=True)

    customer_id = models.CharField(verbose_name='客户编号', max_length=50, blank=True, null=True)

    sales_personnel = models.CharField(verbose_name='销售人员', max_length=50, blank=True, null=True)   # **
    # sold by wholesale or not
    wholesale = models.BooleanField(verbose_name='是否批发', default=False)

    store_name = models.CharField(verbose_name='商店名', max_length=100, null=True)

    location = models.CharField(verbose_name='地点', max_length=500, blank=True, null=True)       # **

    #def create_sales_record(self, item_id, trade_price, customer, sales_personnel, discount):

    def __str__(self):
    	return self.trade_id

    class Meta:
        verbose_name = '销售记录'

        verbose_name_plural = '销售记录'