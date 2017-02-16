#-*- coding:utf-8 -*-
from __future__ import unicode_literals
from django.db import models

# Create your models here.

class problemTag(models.Model):
    name = models.CharField(max_length=30)

    class Meta:
        db_table = 'problem_tag'

    def __unicode__(self):
        return self.name


class problem(models.Model):
    # require attention when adding new problem
    problem_id = models.IntegerField(primary_key=True, db_index=True)

    title = models.CharField(unique=True, max_length=50)

    content = models.TextField(max_length=10000)

    difficulty = models.IntegerField()
	
    example = models.TextField(blank=True)
	# 创建时间
    create_time = models.DateTimeField(auto_now_add=True)

    submission_number = models.IntegerField(default=0)

    accept_number = models.IntegerField(default=0)

    acceptance = models.FloatField(default=0)

    accept_user_number = models.IntegerField(default=0)
    # 提示
    hint = models.TextField(blank=True, null=True)
    # 是否可见 false的话相当于删除
    visible = models.BooleanField(default=True)

    # 时间限制 单位是毫秒
    time_limit = models.IntegerField()
    # 内存限制 单位是MB
    memory_limit = models.IntegerField()
    
    # 标签
    # tags = models.ManyToManyField(problemTag)

    def __unicode__(self):
        return self.title

    class Meta:
        db_table = "problem"


'''
class problem_status(models.Model):
    
    problem_id = models.IntegerField(default=rand_str, db_index=True)

    user_name = models.CharField(max_length=30, db_index=True)

    status = models.IntegerField(default=0)

    ordering = ('user_name', 'problem_id',)

    def __unicode__(self):
        return str(problem_id) + ':' + str(status)
'''



