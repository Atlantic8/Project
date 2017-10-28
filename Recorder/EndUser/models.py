#-*- encoding:utf-8 -*-
from __future__ import unicode_literals
from django.contrib.auth.models import AbstractUser
from django.db import models
import time


def decode(info):
    return info.decode('utf-8')

# Create your models here.

# sales store's information
class Store(models.Model) :
    
    #store_id = models.CharField(max_length=4, primary_key=True, unique=True)

    store_name = models.CharField(verbose_name='商店名' ,max_length=30, primary_key=True, unique=True, null=False)

    store_address = models.CharField(verbose_name='地址' ,max_length=100, default='')

    contact = models.CharField(verbose_name='联系方式', max_length=40)

    create_time = models.DateTimeField(verbose_name='创建时间', auto_now_add=True)

    def __str__(self):
    	return self.store_name+' : '+self.store_address

    class Meta:
        verbose_name = '商店'

        verbose_name_plural = '商店'

# account for company members
class EndUser(AbstractUser):
    
    email = models.EmailField(verbose_name='电子邮件', max_length=50, primary_key=True, unique=True)

    create_time = models.DateTimeField(verbose_name='创建时间', auto_now_add=True)
    # 1: normal salesman, 2: wholesale, 3: administrator
    permission = models.CharField(verbose_name='权限(3:管理员, 2:批发员, 1:零售员)', max_length=2, default='3')
    # which store this personnel belongs to, only for permission=1
    store_name = models.CharField(verbose_name='商店名称', max_length=20, null=True)

    discount = models.CharField(verbose_name='折扣', max_length=4, default='100')

    cart = models.CharField(verbose_name='购物车', max_length=1000, default='', blank=True) # iterm_id linked by #

    contact = models.CharField(verbose_name='联系方式', max_length=100, null=True, blank=True)

    def __str__(self):
    	return self.username

    class Meta:
        verbose_name = '员工'

        verbose_name_plural = '员工'


# account for customers
class Customer(models.Model) :
    
    customer_id = models.CharField(verbose_name='客户编号', max_length=10, primary_key=True, unique=True, default=str(int(time.time())))

    create_time = models.DateTimeField(verbose_name='创建时间', auto_now_add=True)

    # is vip or not, not for wholesale
    vip = models.BooleanField(verbose_name='VIP', default=False)
    # wholesale or not
    wholesale = models.BooleanField(verbose_name='是否批发', default=True)

    customer_name = models.CharField(verbose_name='客户姓名', max_length=30, unique=True)

    contact = models.CharField(verbose_name='联系方式', max_length=40)

    address = models.CharField(verbose_name='地址', max_length=100, default='')

    info = models.CharField(verbose_name='其他信息', max_length=100, default='none')

    def __str__(self):
    	return self.customer_name

    class Meta:
        verbose_name = '客户'

        verbose_name_plural = '客户'



