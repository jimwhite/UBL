#!/usr/bin/perl

$dir = $ARGV[0];
 
$files="";
read_files("./".$dir);
$sum=0.0;
$num_files = 0;
@run_count=();	
@rec_sums=();
@emprec_sums=();
@pre_sums=();
@emppre_sums=();
@f1_sums=();
@empf1_sums=();

foreach $file (split /:/,$files){
    open IN,"$file";
    $num_files++;

    $i=-1;
    $rec=0;
    $emp_rec=0;
    $pre=0;
    $emp_pre=0;
    $f1=0;
    $emp_f1=0;

    while ($line = <IN>){
	# Recall : 21/25 = 0.84
	if ($line=~m/^Recall : \d+\/\d+ = (.+)/){
	    $i++;	    
	    $rec = $1;
	    $run_count[$i]++;
	    $rec_sums[$i]+=$rec;
	}
	if ($line=~m/^EMPTY Recall : \d+\/\d+ = (.+)/){
	    $emp_rec = $1;
	    $emprec_sums[$i]+=$emp_rec;
	}
	if ($line=~m/^Precision : \d+\/\d+ = (.+)/){
	    $pre = $1;
	    $pre_sums[$i]+=$pre;
	}
	if ($line=~m/^EMPTY Precision : \d+\/\d+ = (.+)/){
	    $emp_pre = $1;
	    $emppre_sums[$i]+=$emp_pre;
	}
	if ($line=~m/^F1: (.+)/){
	    $f1 = $1;
	    $f1_sums[$i]+=$f1;
	}
	if ($line=~m/^EMPTY F1: (.+)/){
	    $emp_f1 = $1;
	    $empf1_sums[$i]+=$emp_f1;
	}
	

    }   
    print "$file : ";
    if ($rec!=0.0){
	$round = sprintf("%0.3f", $rec);
	print "r=$round ";   
    }
    if ($pre!=0.0){
	$round = sprintf("%0.3f", $pre);
	print "p=$round ";   
    }
    if ($f1!=0.0){
	$round = sprintf("%0.3f", $f1);
	print "f=$round ";   
    }
    if ($rec!=0.0){
	$round = sprintf("%0.3f", $emp_rec);
	print "  er=$round ";   
    }
    if ($pre!=0.0){
	$round = sprintf("%0.3f", $emp_pre);
	print "ep=$round ";   
    }
    if ($f1!=0.0){
	$round = sprintf("%0.3f", $emp_f1);
	print "ef=$round ";   
    }

    print "\n";
}

for ($i=0; $i<scalar(@run_count); $i++){
    $avg_rec = sprintf("%0.3f",$rec_sums[$i]/$run_count[$i]);
    $avg_pre = sprintf("%0.3f",$pre_sums[$i]/$run_count[$i]);
    $avg_f1 = sprintf("%0.3f",$f1_sums[$i]/$run_count[$i]);
    $is = sprintf("%2d",$i);
    if ($avg_rec!=0.0){
	print "$is : r=$avg_rec p=$avg_pre f=$avg_f1 ";
    }

    $empavg_rec = sprintf("%0.3f",$emprec_sums[$i]/$run_count[$i]);
    $empavg_pre = sprintf("%0.3f",$emppre_sums[$i]/$run_count[$i]);
    $empavg_f1 = sprintf("%0.3f",$empf1_sums[$i]/$run_count[$i]);
    if ($empavg_rec!=0.0){
	print " --- er=$empavg_rec ep=$empavg_pre ef=$empavg_f1 ";
    }

    print "(from $run_count[$i] runs)\n";
}

sub read_files {
    my $dir = (shift);
    my @f = <$dir/*>;
    foreach my $file (@f) {
	if (-d $file) {
	    read_files($file,$names);
	} else {
	    if ($file =~ m/\d$/){
		$files.=$file.":";
	    }
	    #print "$file\n";
	}
    }
} 
