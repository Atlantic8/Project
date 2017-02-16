# -*- coding:utf-8 -*-

from django.shortcuts import render
from django.shortcuts import render_to_response
from account.models import account
from django.http import HttpResponseRedirect
from django.contrib import auth
#from account.auth import MyCustomBackend as backend

# Create your views here.

def login(request):
	if 'submit' not in request.POST:
		return render_to_response('login.html')
	operation = request.POST.get('submit','')
	if operation == 'register':
		return render_to_response('register.html')
	elif operation == 'login':
		username = request.POST.get('username','')
		pwd = request.POST.get('password','')
		'''
		user = auth.authenticate(username=username,password=pwd)
		
		if user is not None and not user.is_forbidden:
			auth.login(request,user)
			return render_to_response('oj/problemset.html',{'username':username})
		else:
			return render_to_response('login.html')
		'''
		user = account.objects.get(user_name=username)
		pwd = user.password

		if pwd and pwd==request.POST.get('password',''):
			request.session['is_login'] = True
			request.session['username'] = username
			# return render_to_response('oj/problemset.html', {'username':username})
			return HttpResponseRedirect('/problemset/page/1/')
		else:
			return render_to_response('login.html')
		


def register(request):
	operation = request.POST.get('submit','')
	if operation == 'cancel':
		return render_to_response('login.html')
	elif operation == 'register':
		Username = request.POST.get('username','')
		Nickname = request.POST.get('nickname','')
		Password = request.POST.get('password','')
		Confpwd  = request.POST.get('confpwd','')
		Email    = request.POST.get('email','')
		# if we access data not in database
		# exception 'matching query does not exist' raises
		try:
			user = account.objects.get(user_name=Username)
			if user is not None:
				return render_to_response('register.html')
		except:
			new_user = account(user_name=Username, \
						nick_name=Nickname, \
						is_forbidden=False, \
						password=Password, \
						email=Email)
			new_user.save()
			return render_to_response('login.html')


def logout(request):
	try:
		del request.session['username']
		del request.session['is_login']
	except:
		print ('can not delete username')
	return render_to_response('login.html')