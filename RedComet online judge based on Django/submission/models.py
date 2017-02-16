# -*- coding:utf-8 -*-
from __future__ import unicode_literals
from django.db import models
from result import result
import hashlib, os

# Create your models here.

def rand_str(length=32):
    if length > 128:
        raise ValueError("length must <= 128")
    return hashlib.sha512(os.urandom(128)).hexdigest()[0:length]

class submission(models.Model):

    id = models.CharField(max_length=32, default=rand_str, primary_key=True, db_index=True)

    user_name = models.CharField(max_length=30, db_index=True)

    problem_id = models.IntegerField(db_index=True)

    title = models.CharField(max_length=50)

    create_time = models.DateTimeField(auto_now_add=True)

	# 判题所用时间
    time_used = models.IntegerField(blank=True, null=True)
    # c, c++, java, python
    language = models.CharField(max_length=6)
    # waiting, judging...
    res = models.IntegerField(default=result["Waiting"])
    # 存储判题结果，比如cpu时间内存之类的, should be json-type string
    memory_used = models.IntegerField(blank=True, null=True)

    # path = models.CharField(max_length=40, default="")

    class Meta:
        db_table = "submission"
        ordering = ['-create_time', 'problem_id']

    def __unicode__(self):
        return 'submission '+self.id