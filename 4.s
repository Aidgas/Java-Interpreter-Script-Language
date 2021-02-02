func m10(value)
{
	return value * 10;
}

var t = 2, k = "45", l = 5, hh =  [2, 5, 88-8, "string"]  ;

hh [ 1 ] += 20;

print( hh [ 1 ] );

hh[2] = toFloat (1 + 6 + 3 * 6/6 + 5 + 2);

print (hh[2]);

print ( hh[ 2 ] * hh[2] );

print( m10(pi3) );
print (k);

func name1(k = 2, l, h)
{
	return (k ^ l) / (h);
}

printList ("name1", toInt ( name1(9, 2, 1) ) );

var start = currentTimeMillis();

print(start);

var h = 0, x = 0, y = 0;
while(h < 360)
{
	h = h + 1;
	x = 0 + 10 * sin(toRadians(h));
	y = 0 + 10 * cos(toRadians(h));
	
	printList(x, y, h);
	
	if( h >= 5 )
	{ 
		break;
	}
}

print(h);

var hh = (currentTimeMillis() - start) / 1000;

print(hh + " second's");

print(m10(hh));

printInt(5, 4, 5, 6);
