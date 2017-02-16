from JudgeRequest import JudgeRequest
from Judger import judge


def Judge (language, code, problemId, time_limit, memory_limit, submissionId):
	'''
	ret should be: 
	state: code(state code in result.py)
	if ac: memory(memory usage), time(judge time)
	'''
	# print (submissionId)

	request = JudgeRequest(language, code, problemId, time_limit, memory_limit, submissionId)
	request.src_path = request.visit()
	request.write_file()

	ret = judge(request.src_path, request)

	ret['path'] = request.src_path

	#print (ret['state'])

	return ret


