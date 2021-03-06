for i in `seq 0 59`
do

	##########################################333333

	file="mc/rate_r_h2.txt"
	mcast=""
	while IFS2=: read -r f1 f2 f3
	do

		f1_=${f1/./,}
		f1_=$( printf "%.0f" $f1_ )

		if [ "$i" == "$f1_" ] ; then
			mcast="$f3" 
			break
		fi
				        
	done <"$file"

	##########################################333333

	file="classico/rate_r_h2.txt"
	classico=""
    while IFS2=: read -r f1 f2 f3
	do
		f1_=${f1/./,}
		f1_=$( printf "%.0f" $f1_ )

		if [ "$i" == "$f1_" ] ; then
			classico="$f3" 
			break
		fi
				        
	done <"$file"

	##########################################333333

	file="classico/rate_s_h2.txt"
	send=""
    while IFS2=: read -r f1 f2 f3
	do
		f1_=${f1/./,}
		f1_=$( printf "%.0f" $f1_ )

		if [ "$i" == "$f1_" ] ; then
			send="$f3" 
			break
		fi
				        
	done <"$file"

	##########################################333333

	printf '%s\t%s\t%s\t%s\n' "$i" "$mcast" "$classico" "$send"

done
