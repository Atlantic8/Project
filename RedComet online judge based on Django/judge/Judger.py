from JudgeRequest import JudgeRequest
from result import result
from settings import settings
from Compiler import compile
import json
import lorun, shlex
import os, sys #, filecmp
import multiprocessing
import types
# filecmp for comparing two files are the same


# compare if output and correct output are the same
def compare_output(output_path, test_case_correct_path):
	ret = ''
	try:
    	# delete '\r' and space, line-switching ch at the end of row
		f1 = file(output_path).read().replace('\r', '').rstrip()
		f2 = file(test_case_correct_path).read().replace('\r', '').rstrip()
	except:
		ret = 'System_Error'

	if f1 == f2:
		ret = 'Accepted'
	elif f1.split() == f2.split():
		ret = 'Format_Error'
	else:
		ret = 'Wrong_Answer'

	# os.remove(output_path)
	return ret


# judge on one test_case, get output
# exe_path is executable file path
def judge_one(exe_path, id, request):
	ret = {}
	#print ('suck my balls')
	# output result path of each test case
	output_path = os.path.join(settings['src_submission_root'], '%s/%d.output'%(request.submission_id,id))
	#print (output_path)
	# path of test case and corresponding correct output, 1.test & 1.output
	test_case_path_root = os.path.join(settings['test_case_root'], str(request.problem_id))
	test_case_correct_path = '%s/%d.output' % (test_case_path_root, id)
	test_case_path = '%s/%d.test' % (test_case_path_root, id)
	#print (test_case_path_root)
	#print (test_case_correct_path)

	input_data = open(test_case_path)
	output_temp_data = open(output_path, 'w')

	main_exe_path = []

	if request.language == 'java':
		main_exe = shlex.split('java -cp %s Main' % exe_path[:-10])
	elif request.language == 'python':
		#main_exe = shlex().split('python2 %s' % exe_path)
		main_exe = shlex.split('python2 %s' % exe_path)
	else:
		main_exe = shlex.split(exe_path)
	# print (main_exe_path)
	
	runcfg = {
        'args': main_exe,
        'fd_in': input_data.fileno(),
        'fd_out': output_temp_data.fileno(),
        'timelimit': request.time_limit,  # in MS
        'memorylimit': request.memory_limit*1024,  # in KB
    }

	res_j = lorun.run(runcfg)
	
	input_data.close()
	output_temp_data.close()


	if res_j['result'] != 0:
		if res_j['result'] == 5:
			ret['state'] = result['Runtime_Error']
		elif res_j['result'] == 2:
			ret['state'] = result['Time_Limit_Exceeded']
		elif res_j['result'] == 3:
			# print ('-------------max memory is:', res_j['memoryused'])
			ret['state'] = result['Memory_Limit_Exceeded']
		return ret

	# compare output with the correct one
	res_c = compare_output(output_path, test_case_correct_path)
	ret['state'] = result[res_c]
	if res_c != 'Accepted':
		return ret

	ret['state'] = result['Accepted']
	ret['time'] = res_j['timeused']
	ret['memory'] =  res_j['memoryused']


	return ret



# file is the code file
def judge(file, request):
	ret = {}
	# compile, exectable file in /data/source_code
	exe_dir  = request.get_dir()
	if request.language != 'java':
		exe_path = os.path.join(exe_dir, 'main')
	else:
		exe_path = os.path.join(exe_dir, 'Main')

	if request.language == 'python':
		exe_path += '.pyc'
	elif request.language == 'java':
		exe_path += '.class'

	state, info = compile(exe_dir, request.language)

	# print ('compile finished----------------------------')

	if state is False:
		ret['state'] = result['Compile_Error']
		return ret

	# compile succeed
	# total number of test cases, divide by two(half of them are executable files)
	TEST_NUMBER = len(os.listdir('%s%d/'%(settings['test_case_root'], request.problem_id))) / 2
	
	# print ('------------------------'+str(TEST_NUMBER))

	pool = multiprocessing.Pool(processes=settings['max_running_number'])
	# record maximum judge time and memory
	max_time, max_memory = 0, 0
	_ress = []
	for id in range(TEST_NUMBER):
		_ress.append(pool.apply_async(judge_one, (exe_path, id, request)))

	pool.close()
	pool.join()
	# os.remove(exe_path)

	ress = []
	for res in _ress:
		ress.append(res.get(timeout=1000))
    
	for res in ress:
		# print (res)
		#print (type(res.get(timeout=1)))

		if res['state'] != result['Accepted']:
			return res
		max_time = max(max_time, res['time'])
		max_memory = max(max_memory, res['memory'])

	ret['state'] = result['Accepted']
	ret['time'] = max_time
	ret['memory'] = max_memory

	return ret


