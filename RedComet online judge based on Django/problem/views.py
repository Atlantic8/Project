# -*- coding:utf-8 -*-

from django.shortcuts import render,render_to_response
from problem.models import problem
from account.models import account
from submission.models import submission
from django.template import Context
from judge.JudgeEntry import Judge
import json
from django.http import HttpResponse
from result import result, rev_result
from django.contrib.auth.decorators import login_required
from django.http import HttpResponseRedirect
from django.contrib import auth
from django.contrib.auth import logout
# from django import forms
# Create your views here.

states_str = ['NT','AC','NA']


def problemset(request, page):

	username = request.session.get('username','')
	if not request.session.get('is_login',False) or username == '':
		return HttpResponseRedirect('/accounts/login/')
	
	page = int(page)
	npp = 20
	all_problem_count = problem.objects.all().count()
	si = npp*(page-1)
	ei = npp*page if npp*page < all_problem_count else all_problem_count

	problems_list = problem.objects.values("problem_id","title","difficulty","acceptance").all()[si:ei]
	#username = request.session['username']

	json_data = account.objects.values("problems_status").get(user_name=username).get('problems_status','{}')

	status_all = json.loads(json_data)

	print (status_all.keys())

	global states_str
	status = []
	problem_ret = []
	for x in range(si,ei):
		# print (str(x+1))
		state = states_str[int(status_all.get(str(x+1)))] if str(x+1) in status_all.keys() else 'NT'
		problem_ret.append(problems_list[x])
		problem_ret[int(x)]['state'] = state

	#hahah = problems_list[0]
	#hahah['text'] = 'hello world'
	# print (problem_ret)

	go_prev = page-1 if page>1 else 0
	go_next = page+1 if ei<all_problem_count else 0
	return render_to_response('oj/problemset.html', {'problem_set':problem_ret, \
		'go_prev':go_prev,'go_next':go_next, 'username':username, 'page':page})


def problem_page(request, cur_title):

	username = request.session.get('username','')

	if not request.session.get('is_login',False) or username=='':
		return HttpResponseRedirect('/accounts/login/')

	cur_problem = problem.objects.get(title=cur_title)
	request.session['problem_id'] = cur_problem.problem_id
	request.session['problem_title'] = cur_title
	#username = request.session['username']
	return render_to_response('oj/problem.html', {'problem':cur_problem, 'username':username,\
	 'code':'// input your code here'} )


def submit(request):
	username = request.session.get('username','')
	if username == '':
		return HttpResponseRedirect('/accounts/login/')
	#print ('---------------------------------------------------------')

	lang = request.POST.get('lang','')
	code = request.POST.get('code','')

	# print (str(lang))

	#username = request.session['username']
	problemId = int(request.session['problem_id'])
	cur_problem = problem.objects.get(problem_id=problemId)
	problemTitle = request.session['problem_title']
	# query problem info from database
	limit = problem.objects.values("time_limit", "memory_limit").get(problem_id=problemId)
	time_limit = limit.get('time_limit',1000)
	memory_limit = limit.get('time_limit',1)
	# debug print (time_limit)
	# create a new submission
	new_submission = submission(user_name=username, problem_id=problemId, title=problemTitle,language=lang)
	# compile & judge code, return full result, including location of each submission
	# keys include state, [time, memory]
	sub_res = Judge(lang, code, problemId, time_limit, memory_limit, new_submission.id)
	new_submission.res = sub_res['state']
	# new_submission.path = sub_res['path']

	if sub_res['state'] == 0:
		new_submission.time_used = sub_res['time']
		new_submission.memory_used = sub_res['memory']

	# insert new submission to database
	new_submission.save()

	# update info of account profile
	flag = False # used to indicate whether a problem is solved which was not solved before
	profile = account.objects.get(user_name=username)
	# print (type(profile.problems_status))
	js = profile.problems_status

	# print (js.get(str(problemId), -1))

	if js.get(str(problemId), -1) != '1':
		(js[problemId], flag) = (1, True) if sub_res['state']==result['Accepted'] else (2, False)
	profile.problems_status = js
	profile.save()

	# update problem info, say, acceptance. etc
	prob = problem.objects.get(problem_id=problemId)
	prob.submission_number += 1
	if sub_res['state']==result['Accepted']:
		prob.accept_number += 1
		if flag:
			prob.accept_user_number += 1
	prob.acceptance = 1.0*prob.accept_number/prob.submission_number
	prob.save()

	# render_to_response('oj/problem.html', {'problem':cur_problem, 'username':username, \
		#'judged':True, 'result': rev_result[sub_res['state']], 'code':code})
	return HttpResponse(rev_result[sub_res['state']])


def logout(request):
	try:
		request.session['username'] = ''
		request.session['is_login'] = False
		#auth.logout(request)
	except:
		print ('can not delete username')
	return HttpResponseRedirect('/accounts/login/')


