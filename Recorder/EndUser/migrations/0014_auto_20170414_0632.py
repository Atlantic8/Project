# -*- coding: utf-8 -*-
# Generated by Django 1.10.1 on 2017-04-14 06:32
from __future__ import unicode_literals

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('EndUser', '0013_auto_20170414_0631'),
    ]

    operations = [
        migrations.AlterField(
            model_name='customer',
            name='customer_id',
            field=models.CharField(default=b'1492151542', max_length=10, primary_key=True, serialize=False, unique=True),
        ),
    ]