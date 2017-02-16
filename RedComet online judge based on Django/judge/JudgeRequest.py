import os
from settings import settings

class JudgeRequest:

	def __init__(self, language, code, problemId, time_limit, memory_limit,  submissionId):
		
		self.language = language
		self.code = code
		# self.username = username
		self.problem_id = int(problemId)
		self.memory_limit = int(memory_limit)
		self.time_limit = int(time_limit)
		self.submission_id = submissionId

		if language in ['java', 'python']:
			self.memory_limit = int(self.memory_limit * 1.5)
			self.time_limit = int(self.time_limit * 1.5)


		# create dir and file, write code into file 
		self.src_path = ''
		# write_file(self.src_path, code)

	def visit(self):
		# current dir is judge/
		'''
		dir = self.visit_dir('%s/%s/' % (settings['src_code_root'],self.username))
		dir = self.visit_dir('%s%s/' % (dir, str(self.problem_id)))
		dir = self.visit_dir('%s%s/' % (dir, self.language))
		dir = self.visit_dir('%s%s/' % (dir, self.submission_id))
		file = '%smain' % dir
		'''
		dir = self.visit_dir('%s%s/' % (settings['src_submission_root'], self.submission_id))
		self.visit_dir(dir)
		if self.language != 'java':
			file = os.path.join(dir, 'main')
		else:
			file = os.path.join(dir, 'Main')
		# get postfix name
		if self.language=='c':
			file = file + '.c'
		elif self.language=='c++':
			file = file + '.cpp'
		elif self.language=='java':
			file = file + '.java'
		elif self.language=='python':
			file = file + '.py'
		#print (file)
		#print (os.getcwd())
		return file

	def visit_dir(self, dir):
		if not os.path.exists(dir):
			os.makedirs(dir)
		return dir

	def read_file(self, fp):
		ret = ''
		for data in fp:
			ret = ret+data
		fp.close()
		return ret

	def write_file(self):
		fp = open(self.src_path,'w')
		fp.write(self.code.encode("utf8"))
		fp.close()

	def get_dir(self):
		dir = os.path.join(settings['src_submission_root'], self.submission_id)
		return dir+'/'