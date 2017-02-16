# -*- coding:utf-8 -*-
import subprocess

def compile(exe_dir, language): # file_path, exe_path,
    '''将程序编译成可执行文件'''
    build_cmd = {
        "c"    : "gcc main.c -o main -Wall -lm -O2 -std=c99 --static -DONLINE_JUDGE",
        "c++"    : "g++ main.cpp -O2 -Wall -lm --static -DONLINE_JUDGE -o main",
        "java"   : "javac Main.java",
        "ruby"   : "ruby -c main.rb",
        "perl"   : "perl -c main.pl",
        "pascal" : 'fpc main.pas -O2 -Co -Ct -Ci',
        "go"     : '/opt/golang/bin/go build -ldflags "-s -w"  main.go',
        "lua"    : 'luac -o main main.lua',
        "python": 'python2 -m py_compile main.py',
        "python3": 'python3 -m py_compile main.py',
        "haskell": "ghc -o main main.hs",
    }
    '''
    if language=='java' :
        cmd = build_cmd[language].format(src_path=file_path)
    else:
        cmd = build_cmd[language]#.format(src_path=file_path, target_path=exe_path)
    '''
    cmd = build_cmd[language]
    # cwd is the working directory, source file and exe are stored in cwd
    p = subprocess.Popen(cmd,shell=True,cwd=exe_dir,stdout=subprocess.PIPE,stderr=subprocess.PIPE)
    # print (exe_dir)
    out,err =  p.communicate()#获取编译错误信息
    if p.returncode == 0: #返回值为0,编译成功
        return True,''
    # print ('-----------------------------------')
    # return false and related error info
    return False,err+out