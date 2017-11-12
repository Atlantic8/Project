# _*_ coding: UTF-8 _*_

# 配置文件的以类的单位，类的首行如下：

# #   class_name  n
# 第一个字符是#，后面跟着类的名称class_name，然后是这个类的直接元素个数n 接下来的n行，每一行代表一个直接元素，其组成如下

# name   type    is_necessary    is_array    default_value   class_name    is_ptr
# 其中：

# name是当前元素的名称
# type是当前元素的类型，可以有int、string、object三种
# is_necessary表示当前元素是否必须，必须的元素必须存在，非必须的则可以没有
# is_array表示当前元素是否是数组
# default_value表示当前元素的默认值，只在元素非必须的时候有效，只针对基本类型
# class_name标识当前元素的类型名称，仅在type为object时有效
# is_ptr只在type为object时有用。为1：是指针，0则不是

# 局限

# 不支持嵌套数组
# 第一层就是数组的不支持，需要单独处理

class element:
    def __init__(self, name=None, type=None, necessary=None, \
        array=None, default_value=None, class_name=None, mapped_name=None):
        self.name = name
        self.type = type
        self.necessary = necessary
        self.array = array
        self.default_value = default_value
        self.class_name = class_name
        self.mapped_name = mapped_name
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
            line = fp.readline().strip('\n')
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
        if len(conf_data) < 6:
            print ('you may need to check configure file.')
            print ('attention, wrong conf_file format: ', conf_data, len(conf_data))
            return None

        if root_p.sons.has_key(conf_data[0]):
            print ('Warning: duplicate attribute name.')
            return False
        new_ele = element(conf_data[0], conf_data[1], \
                conf_data[2], conf_data[3], conf_data[4], conf_data[5])
        new_ele.mapped_name = conf_data[6] if len(conf_data)>6 else new_ele.name
        #new_ele.ptr = '1' if len(conf_data)>6 and conf_data[6].strip('\n')=='1' else '0'
        
        root_p.sons[conf_data[0]] = new_ele

        return True

        

