#!/usr/bin/env python
#coding: utf8

import struct
from citycode import CITIES as cities 

f = open('phonenum.txt')

firstsee = (None, None)
step = 0

# output to raw
r = open('../res/raw/phonenumber.bin', 'wb')

for l in f.readlines():
	prefix , city = l.split('\t')
	prefix = int(prefix) - 1300000
	city = city.strip()

	if firstsee[1] != city or firstsee[0] != prefix - step - 1:
		if firstsee[1]:
			r.write(struct.pack('>IHH', firstsee[0] , step, cities[firstsee[1]]))

		step = 0
		firstsee = (prefix, city)

	else:
		step += 1
r.close()
