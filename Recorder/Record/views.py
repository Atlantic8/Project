# -*- coding: utf-8 -*-
from django.shortcuts import render, render_to_response
from Record.models import product, sales_record
from EndUser.models import Customer, Store, EndUser
#from django.core.files.base import ContentFile
from django.contrib.auth.decorators import login_required
from django.http import HttpResponseRedirect, HttpResponse
#from xhtml2pdf import pisa as pisa
import datetime
import os
from Recorder import settings
from django.template import Template, Context
from django.template.loader import get_template
from django.contrib.auth import authenticate
from EndUser.views import logout
from django.db.models import Q
'''
try:
    import StringIO
    StringIO = StringIO.StringIO
except:
    from io import StringIO
'''

# Create your views here.

def read_announcement():
    f = open(settings.BASE_DIR+'/Record/announcement.dat', 'r')
    all_the_text =f.read()
    f.close()
    return all_the_text

def write_announcement(data):
    try:
        f = file(settings.BASE_DIR+'/Record/announcement.dat', 'w+')
        f.write(data)
        f.close()
        return True
    except:
        return False

@login_required
def reset_announcement(request):
    ret = 1
    try:
        new_ann = request.POST.get('new_ann', '')
        res = write_announcement(new_ann)
        if res == False:
            print ('write failed')
            ret = 0
    except:
        ret = 0
    return HttpResponse(ret)

@login_required
def get_single_record(request, serial_no):  # get detail information of item with serial_no serial_no 
    single_record = product.objects.get(serial_no=serial_no)
    username = request.user.username
    user = EndUser.objects.get(username=username)

    announcement = read_announcement()

    store = {}
    if user.store_name is not single_record.store_name:
        try:
            store = Store.objects.get(store_name=single_record.store_name)
        except:
            print ('invalid store name.')
    return render_to_response('single_record.html', {'username' : username, "xbox" : announcement, \
		                        'single_record' : single_record, 'user' : user, 'store' : store })



# get n-th page's records
@login_required
def get_record(request, page):
    # print(page)
    # print ('suck my balls')
    page = int(page)
    # parameters need to be taken
    prev, next, curr = False, False, page
    username = request.user.username
    permission = int(request.user.permission)
    user = EndUser.objects.get(username=username)
    #admin = True if permission == 3 else False

    announcement = read_announcement()

    if permission == 3: # admin
        total_record_count = product.objects.all().count()
        npp = 16
        # records in page
        sp = npp * (page-1)
        ep = npp*page if npp*page<total_record_count  else total_record_count

        prev = page-1 if page>1 else -1
        next = page+1 if ep<total_record_count else -1

        # .values('image','serial_no','description','report_no',\
        #	'category','pieces', 'size','main_material_name','weight','main_stone_weight',\
        #	'general_information','comment','sales_price','')

        data_product = product.objects.all()[sp:ep]

        is_null = True if len(data_product)==0 else False
    
        return render_to_response('test_record.html', {'records':data_product,'page':page,'user' : user,\
    	'prev':prev, 'next':next,'username':username, 'xbox':announcement, 'is_null':is_null })
    elif permission == 2: # wholesale
        total_record_count = product.objects.filter(wholesale=False).count()
        npp = 16
        # records in page
        sp = npp * (page - 1)
        ep = npp * page if npp * page < total_record_count  else total_record_count

        prev = page - 1 if page > 1 else -1
        next = page + 1 if ep < total_record_count else -1

        # .values('image','serial_no','description','report_no',\
        #	'category','pieces', 'size','main_material_name','weight','main_stone_weight',\
        #	'general_information','comment','sales_price','')

        data_product = product.objects.filter(wholesale=False)[sp:ep]

        is_null = True if len(data_product) == 0 else False

        return render_to_response('test_record.html', {'records': data_product, 'page': page, 'xbox':announcement, \
                                'user' : user,'prev': prev, 'next': next, 'username': username, 'is_null':is_null})
    elif permission == 1: # retail
        total_record_count = product.objects.filter(wholesale=False).count()
        npp = 16
        # records in page
        sp = npp * (page - 1)
        ep = npp * page if npp * page < total_record_count  else total_record_count

        prev = page - 1 if page > 1 else -1
        next = page + 1 if ep < total_record_count else -1

        # .values('image','serial_no','description','report_no',\
        #	'category','pieces', 'size','main_material_name','weight','main_stone_weight',\
        #	'general_information','comment','sales_price','')

        data_product = product.objects.filter(wholesale=False)[sp:ep]

        is_null = True if len(data_product) == 0 else False

        return render_to_response('test_record.html', {'records': data_product, 'page': page, 'xbox':announcement, \
                                'user' : user,'prev': prev, 'next': next, 'username': username, 'is_null':is_null})




@login_required
def main_site(request):
    return HttpResponseRedirect("/records/1/")


@login_required
def remove_from_cart(request, item_id):  # ajax function
    ret = 0
    try :
        user = EndUser.objects.get(username=request.user.username)
        cart_list = user.cart.split("#")
        if item_id not in cart_list:
            raise Exception('Remove non-existing product')
        cart_list.remove(item_id)
        data = ''
        for cart in cart_list:
            data += '#'+cart

        user.cart = data if len(data)==0 else data[1:]
        user.save()
    except:
        print ('Exception encounted')
    finally:
        return get_cart(request)

@login_required
def remove_from_cart1(request, item_id):  # ajax function
    ret = 0
    # print ('suck my ball')
    try :
        user = EndUser.objects.get(username=request.user.username)
        cart_list = user.cart.split("#")
        if item_id not in cart_list:
            ret = 0
        cart_list.remove(item_id)
        data = ''
        for cart in cart_list:
            data += '#'+cart

        user.cart = data if len(data)==0 else data[1:]
        user.save()
        ret = 1
    except:
        print ('Exception encounted')
        ret = 0
    finally:
        return HttpResponse(ret)


@login_required
def get_cart(request):
    # return with customers list and cart products list
    username = request.user.username
    user = EndUser.objects.get(username=request.user.username)
    strs = user.cart

    announcement = read_announcement()
    print (announcement)

    if len(strs)==0:
        return render_to_response('cart.html', {'records':[],'user' : user,\
        'name_list':[], 'username':username, 'empty':True, 'xbox':announcement})
    cart_list = strs.split("#")
    records = []
    for item_id in cart_list:
        item_id = item_id.strip()
        if len(item_id) > 0:
            records.append(product.objects.get(item_id=item_id))

    name_list = Customer.objects.all().values('customer_name')

    return render_to_response('cart.html', {'records':records,'user' : user,\
        'name_list':name_list, 'username':username, 'empty':False, 'xbox':announcement})

@login_required
def add_to_cart(request): # ajax
    item_id = request.POST.get('product', '')
    ret = 0
    announcement = read_announcement()
    try:
        user = EndUser.objects.get(username=request.user.username)
        #print (user.cart)
        if len(user.cart) == 0:
            user.cart = item_id
        else:
            cart_list = user.cart.split("#")

            if item_id in cart_list:
                ret = 0
            else:
                user.cart += '#'+item_id
        user.save()
        ret = 1
    except:
        print ('Exception encounted')
        ret = 0
    finally:
        return HttpResponse(ret)


@login_required
def buy_cart(request):
    # buy selected products in cart
    # the customer name is customer_name
    buy_list = request.GET.getlist("check_box_list", [])
    if len(buy_list) == 0:
        return HttpResponseRedirect("/records/cart/")
    for prod in buy_list:
        tmp = product.objects.get(item_id=prod).is_sold
        if tmp == True:
            return HttpResponseRedirect("/records/1/")
    customer_name = request.GET.get('customer_name', '')
    # print ('customer:'+customer_name)
    # print (buy_list)
    username = request.user.username
    total_price = 0.0

    # store name and location
    store_instance = None
    try:
        Store.objects.get(store_name=request.user.store_name)
    except:
        pass
    location = '#' if store_instance is None or request.user.permission != '1' else store_instance.store_address
    store_name = store_instance.store_name if request.user.permission == '1' and store_instance is not None else '非零售'
    # wholesale or not
    wholesale = True if request.user.permission != 1 else False

    # trade_name
    trade_name = str(hash(username)) + str(sales_record.objects.all().count() + 1)

    # item name list
    item_name = ''

    # write sales records
    for prod in buy_list:
        item_name += '#'+prod
        # modify product state to 'sold'
        product_instance = product.objects.get(item_id=prod)
        product_instance.is_sold = True
        product_instance.save()
        # discount
        discount = min(float(request.user.discount), float(product_instance.discount)) / 100.0
        # trade price
        trade_price = int(product_instance.sales_price) * discount
        total_price += trade_price * discount

    sr = sales_record(item_id=item_name[1:], customer_id=customer_name, discount=str(discount * 100), \
                      sales_personnel=username, store_name=store_name, \
                      trade_id=trade_name, wholesale=wholesale, trade_price=total_price, \
                      location=location)
    # write sales_record data to database
    sr.save()

    # remove from cart
    user = EndUser.objects.get(username=username)
    str_cart = user.cart
    cart_list = str_cart.split("#")

    for prod in buy_list:
        remove_from_cart1(request, prod)

    # generate receipt
    records = []
    for prod in buy_list:
        records.append(product.objects.get(item_id=prod))
    now_time = datetime.datetime.now()
    time_str = datetime.datetime.strftime(now_time, '%Y-%m-%d %H:%M:%S')
    t = get_template('receipt.html')
    c = Context({'records':records, 'client':customer_name, \
                                    'price':total_price, 'sales_personnel':username, \
                                    'location':location, 'number':len(buy_list), 'date':time_str})
    return render_to_response('receipt.html', c)
    '''
    html_data = t.render(c)
    #html_data = render('receipt.html', {'records':records, 'client':customer_name, \
    #                                'price':total_price, 'sales_personnel':username, \
    #                                'location':location, 'number':len(buy_list), 'date':time_str})
    dest_path = '/media/tmp/'
    filename = settings.BASE_DIR + os.path.join(dest_path, username) + '.pdf'
    if os.path.exists(filename) is False:
        open(filename, 'w')
    
    result = file(filename, 'wb')
    pdf = pisa.CreatePDF(html_data, result)
    result.close()
    # pisa.startViewer('test.pdf')

    # Create the HttpResponse object with the appropriate PDF headers.
    response = HttpResponse(content_type='application/pdf')
    response['Content-Disposition'] = 'attachment; filename=' + filename
    response['Content-Length'] = os.path.getsize(filename)  # 传输给客户端的文件大小

    return response
    
    # print (html_data)
    result = StringIO()
    pdf = pisa.CreatePDF(StringIO(html_data), result)
    pdf = pisa.CreatePDF(StringIO(html_data), result)
    print (settings.BASE_DIR+'/static/css/receipt.css')
    pdf.addCSS(settings.BASE_DIR+'/static/css/receipt.css')
    if not pdf.err:
        return HttpResponse(result.getvalue(), content_type='application/pdf')
    return HttpResponse('We had some errors')
    '''

# get account information
@login_required
def get_account(request):
    announcement = read_announcement()
    username = request.user.username
    user = EndUser.objects.get(username=username)
    print (user.permission)
    return render_to_response('account.html', {'username':username, 'user':user, 'xbox':announcement})

# reset password
@login_required
def reset_password(request):
    ret = 1
    announcement = read_announcement()
    try:
        username = request.user.username
        password = request.POST.get('password', '')
        user = authenticate(username=username, password=password)  # django自带auth验证用户名密码
        if user is None:
            user = EndUser.objects.get(username=username, password=password)
        if user is None:
            return 0
        new_password = request.POST.get('new_password', '')
        user.set_password(new_password)
        user.save()
        logout(request)
    except:
        ret = 0
    return HttpResponse(ret)

# get sales records
@login_required
def get_sales_record(request, page):
    page = int(page)
    username = request.user.username
    announcement = read_announcement()

    user = EndUser.objects.get(username=username)
    npp = 10

    if int(user.permission) == 3:
        total_record_count = sales_record.objects.all().count()
        # records in page
        sp = npp * (page - 1)
        ep = npp * page if npp * page < total_record_count  else total_record_count

        prev = page - 1 if page > 1 else -1
        next = page + 1 if ep < total_record_count else -1

        salesR = sales_record.objects.all()[sp:ep]
        number = []
        for sr in salesR:
            products = sr.item_id.split('#')
            #sr['number'] = len(products)
            my_filter_qs = Q()
            number.append(len(products))
            for prod in products:
                my_filter_qs = my_filter_qs | Q(item_id=prod)
            sr.item_id = product.objects.filter(my_filter_qs)


        srs = zip(salesR, number)
        is_null = True if len(srs)==0 else False
        return render_to_response('sales_record.html', {'page':page, 'prev':prev, 'next':next, 'is_null':is_null,'user' : user,\
                                'number':number, 'username': username, 'srs': srs, 'xbox':announcement})
    else:
        total_record_count = sales_record.objects.filter(sales_personnel=username).count()
        # records in page
        sp = npp * (page - 1)
        ep = npp * page if npp * page < total_record_count  else total_record_count

        prev = page - 1 if page > 1 else -1
        next = page + 1 if ep < total_record_count else -1

        salesR = sales_record.objects.filter(sales_personnel=username)[sp:ep]
        number = []
        for sr in salesR:
            products = sr.item_id.split('#')
            # sr['number'] = len(products)
            my_filter_qs = Q()
            number.append(len(products))
            for prod in products:
                my_filter_qs = my_filter_qs | Q(item_id=prod)
            sr.item_id = product.objects.filter(my_filter_qs)

        srs = zip(salesR, number)
        is_null = True if len(srs) == 0 else False
        return render_to_response('sales_record.html', {'page': page, 'prev': prev, 'next': next, 'is_null':is_null,'user' : user,\
                                                        'number': number, 'username': username, 'srs': srs,
                                                        'xbox': announcement})


# get filtered page's records
@login_required
def get_filtered_record(request, page):
    # print(page)
    page = int(page)
    # parameters need to be taken
    prev, next, curr = False, False, page
    username = request.user.username
    user = EndUser.objects.get(username=username)
    permission = int(request.user.permission)
    # admin = True if permission == 3 else False

    announcement = read_announcement()
    '''
    # 获取过滤条件
    filter_category = request.POST.get("category")
    filter_min_price = int(request.POST.get("min_price"))
    filter_max_price = int(request.POST.get("max_price"))
    filter_discount = int(request.POST.get('check_box_discount'))
    filter_available = int(request.POST.get('check_box_available'))
    filter_max_price = 100000000 if filter_max_price==0 else filter_max_price
    '''
    if permission == 3:  # admin
        #if filter_discount
        data_product = product.objects.all().count()
        total_record_count = len(data_product)
        npp = 16
        # records in page
        sp = npp * (page - 1)
        ep = npp * page if npp * page < total_record_count else total_record_count

        prev = page - 1 if page > 1 else -1
        next = page + 1 if ep < total_record_count else -1

        # .values('image','serial_no','description','report_no',\
        #	'category','pieces', 'size','main_material_name','weight','main_stone_weight',\
        #	'general_information','comment','sales_price','')

        data_product = product.objects.all()[sp:ep]

        is_null = True if len(data_product) == 0 else False

        return render_to_response('test_record.html', {'records': data_product, 'page': page, 'user' : user,\
                                                       'prev': prev, 'next': next, 'username': username,
                                                       'xbox': announcement, 'is_null': is_null})
    elif permission == 2:  # wholesale
        total_record_count = product.objects.filter(wholesale=False).count()
        npp = 16
        # records in page
        sp = npp * (page - 1)
        ep = npp * page if npp * page < total_record_count  else total_record_count

        prev = page - 1 if page > 1 else -1
        next = page + 1 if ep < total_record_count else -1

        # .values('image','serial_no','description','report_no',\
        #	'category','pieces', 'size','main_material_name','weight','main_stone_weight',\
        #	'general_information','comment','sales_price','')

        data_product = product.objects.filter(wholesale=False)[sp:ep]

        is_null = True if len(data_product) == 0 else False

        return render_to_response('test_record.html', {'records': data_product, 'page': page, 'xbox': announcement, \
                                                       'prev': prev, 'next': next, 'username': username,'user' : user,
                                                       'is_null': is_null})
    elif permission == 1:  # retail
        total_record_count = product.objects.filter(wholesale=False).count()
        npp = 16
        # records in page
        sp = npp * (page - 1)
        ep = npp * page if npp * page < total_record_count  else total_record_count

        prev = page - 1 if page > 1 else -1
        next = page + 1 if ep < total_record_count else -1

        # .values('image','serial_no','description','report_no',\
        #	'category','pieces', 'size','main_material_name','weight','main_stone_weight',\
        #	'general_information','comment','sales_price','')

        data_product = product.objects.filter(wholesale=False)[sp:ep]

        is_null = True if len(data_product) == 0 else False

        return render_to_response('test_record.html', {'records': data_product, 'page': page, 'xbox': announcement, \
                                                       'prev': prev, 'next': next, 'username': username,
                                                       'is_null': is_null, 'user' : user})







