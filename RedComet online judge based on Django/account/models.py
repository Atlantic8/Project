#-*-coding:utf-8 -*-
from __future__ import unicode_literals
from django.db import models
from jsonfield import JSONField
#from django.contrib.auth.models import AbstractBaseUser


class account(models.Model):
    #user_id = models.IntegerField(unique=True, primary_key=True, default=1)

    user_name = models.CharField(max_length=30, primary_key=True, unique=True) # primary_key=True
    
    nick_name = models.CharField(max_length=30, default='New user')
    
    password  = models.CharField(max_length=30)
    
    email = models.EmailField(default='')
    
    is_forbidden = models.BooleanField(default=False)
    
    create_time = models.DateTimeField(auto_now_add=True, null=True)
    
    problems_status = JSONField(default="{}",null=True, blank=True)

    #USERNAME_FIELD = 'user_name'

    #REQUIRED_FIELDS = []

    #objects = UserManager()
    
    def __unicode__(self):
        return self.user_name

    class Meta:
        db_table = "user"