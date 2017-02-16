# -*- coding:utf-8 -*-
from django.shortcuts import render, render_to_response
from submission.models import submission
from django.contrib.auth.decorators import login_required
from result import rev_result
from django.http import HttpResponseRedirect

# Create your views here.

def all_submission(request, page=1):

	username = request.session.get('username','')

	if not request.session.get('is_login',False) or username=='':
		return HttpResponseRedirect('/accounts/login/')
	# number of submissions in each page
	page = int(page)
	npp = 20
	
	# count number of all submissions of current user
	all_submission = submission.objects.filter(user_name=username).count()

	if all_submission == 0: # no submissions yet
		return render_to_response('oj/submission.html', {'submissions':-1, 'username':username})

	all_page = all_submission / npp
	if  all_submission % npp != 0:
		all_page += 1
	
	# get all submissions of current user
	si = npp*(page-1)
	ei = npp*page if npp*page < all_submission else all_submission
	submission_list = submission.objects.filter(user_name=username)[si:ei]

	go_prev = 0 if page==1 else page-1
	go_next = 0 if page==all_page else page+1

	return render_to_response('oj/submission.html', {'page':page, 'go_prev':go_prev, \
		'go_next':go_next, 'submissions':submission_list, 'username':username, 'result':rev_result})
		
	

# submission of certain problem for Page: page
def submission_problem(request, page):

	username = request.session.get('username','')

	if not request.session.get('is_login',False) or username == '':
		return HttpResponseRedirect('/accounts/login/')
	
	# number of submissions in each page
	page = int(page)
	npp = 20

	problemId = request.session['problem_id']
	problemTitle = request.session['problem_title']
	# count number of all submissions of current user
	all_submission = submission.objects.filter(user_name=username, problem_id=problemId).count()

	if all_submission == 0: # no submissions yet
		return render_to_response('oj/submission.html', {'submissions':-1, 'username':username})

	all_page = all_submission / npp
	if  all_submission % npp != 0:
		all_page += 1

	# get all submissions of current user
	si = npp*(page-1)
	ei = npp*page if npp*page < all_submission else all_submission
	submission_list = submission.objects.filter(user_name=username, problem_id=problemId)[si:ei]

	go_prev = 0 if page==1 else page-1
	go_next = 0 if page>=all_page else page+1

	return render_to_response('oj/submission.html', {'page':page, 'go_prev':go_prev, \
		'go_next':go_next, 'submissions':submission_list, 'username':username, 'result':rev_result})


# one certain submission info including code
def submission_page(request):
	#username = request.session['username']
	username = request.session.get('username','')
	if not request.session.get('is_login',False) or username == '':
		return HttpResponseRedirect('/accounts/login/')

	id = request.GET.get('id')
	cur_submission = submission.objects.get(id=id)
	if cur_submission.language == 'python':
		code_path_postfix = 'py'
	elif cur_submission.language == 'c++':
		code_path_postfix = 'cpp'
	else:
		code_path_postfix = cur_submission.language
	code_path = 'data/submission/%s/main.%s' % (id, code_path_postfix)

	code = open(code_path).read()
	# print (code)

	return render_to_response('oj/submission_page.html', {'submission':cur_submission, \
		'code':code, 'username':username})






