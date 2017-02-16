import os

class FileManage:

	def __init__(self, username, language, problemId):
		self.username = username
		self.language = language
		self.problem_id = problemId

	def visit_dir(self, dir):
		if not os.path.exists(dir):
			os.makedirs(dir)

	def visit_file(self, file):
		fp = open("test.txt",w)
		return fp

	def read_file(self, fp):
		ret = ''
		for data in fp:
			ret = ret+data
		return ret

	def write_file(self, fp, data):
		fp.write(data)
