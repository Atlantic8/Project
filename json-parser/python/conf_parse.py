# _*_ coding: UTF-8 _*_
# 2017-7-12，加入class name
# 先说明下conf文件的编码规范
# conf文件中可以包含多个class的描述，每个class的第一行为 ： 
# #    class_name  行数
# class_name为类名，行数n表示下面n行都是这个class的规则
# 下面n行为json中第一层的属性(只考虑一层，如果有第二层就递归解析)，格式为（以\t隔开）：
# 变量名    变量类型    是否必须   是否数组    默认值    类名
# 变量类型包括：int, float, string, object
# 是否必须：有 '1':必须，'0':不必须
# 是否数组：有 '1':是，'0':不是
# 默认值：只在不必须且json对应属性不存在时才有效
# 类名：只在变量类型为object时才有用，指示object的类型
class element:
    def __init__(self, name=None, type=None, necessary=None, \
        array=None, default_value=None, class_name=None):
        self.name = name
        self.type = type
        self.necessary = necessary
        self.array = array
        self.default_value = default_value
        self.class_name = class_name
        self.sons = {}
        

class conf_tree:
    def __init__(self):
        # self.root = element('root', '*', '4', '1', '0', 'none')
        self.map = {}
    
    def name_filter(self, name):
        return ''.join(map(lambda x: x if x.isdigit() or x.isalpha() or x is '_' '#' else '', list(name)))

    def construct_tree(self, conf_file_path):
        fp = open(conf_file_path, 'r')
        while True:
            line = fp.readline()
            if not line:
                break
            lines = line.split('\t')
            class_name = lines[1]
            rule_number = int(lines[2])
            #print (rule_number)
            root = element('root', '4', '1', '0', '0', class_name)
            for i in range(rule_number):
                line = fp.readline()
                if self.construct_tree_by_element(root, line) is False:
                    print ('fail to construct element')
            self.map[class_name] = root
            #print (root.sons)
    
    def construct_tree_by_element(self, root_p, ele_data):
        conf_data = ele_data.split('\t')
        # print (ele_data, len(conf_data))
        if len(conf_data) != 6:
            print ('you may need to check configure file.')
            print ('attention, wrong conf_file format: ', conf_data, len(conf_data))
            return None

        if root_p.sons.has_key(conf_data[0]):
            print ('Warning: duplicate attribute name.')
            return False
        new_ele = element(conf_data[0], conf_data[1], \
                conf_data[2], conf_data[3], conf_data[4], conf_data[5].strip('\n'))
        root_p.sons[conf_data[0]] = new_ele

        return True

        

