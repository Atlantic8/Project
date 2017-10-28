#-*-encoding:utf8 -*-
from django.shortcuts import render, render_to_response
from EndUser.models import EndUser
from django.http import HttpResponse,HttpResponseRedirect
from django.contrib.auth import authenticate,login,logout
from django.contrib.auth.decorators import login_required

# Create your views here.

#注册
def register(request):
    error = []
    # if request.method == 'GET':
    #     return render_to_response('register.html',{'uf':uf})
    if request.method == 'POST':
    	username=request.POST.get('username','')
    	password=request.POST.get('password','')
    	password_conf=request.POST.get('password_conf','')
    	email=request.POST.get('email','')
    	if username=='' or password=='' or password_conf=='' or email=='' or password!=password_conf:
            render_to_response('Oops, wrong information, please fill these blanks carefully.')
        
        if not EndUser.objects.all().filter(username=username):
            user = EndUser()
            user.username = username
            user.set_password(password)
            user.email = email
            user.save()
            return render_to_response('login.html')
        else:
        	return HttpResponse("username exists already!!!")
    return HttpResponse("try again please!!!")

def do_login(request) :
    # print('hello')
    if 'submit' not in request.POST:
        return render_to_response('login.html')
    if request.method =='POST':
        #print ('I am working')
        username=request.POST.get('username', '')
        password=request.POST.get('password', '')
        # print (username, password)
        # print (username, password)
        user = None
        try:
            user = authenticate(username=username, password=password)               #django自带auth验证用户名密码
        except:
            pass
        if user is None:
            try:
                user = EndUser.objects.get(username=username, password=password)
            except:
                pass
        if user is not None:                                                  #判断用户是否存在
            if user.is_active:                                                  #判断用户是否激活
                login(request, user)                                                 #用户信息验证成功后把登陆信息写入session
                return HttpResponseRedirect("/records/1/")
            else:
                return render_to_response('disable.html', {'username':username})
        else:
            return HttpResponse("无效的用户名或者密码!!!")
    return render_to_response('login.html')

     
#退出
@login_required
def do_logout(request):
    logout(request)
    return HttpResponseRedirect('/login/')