#!/usr/bin/env perl

use strict;
use warnings;
use utf8;

use CGI qw(:standard);
use Path::Tiny;

binmode(STDIN,  "utf8");
binmode(STDOUT, "utf8");
binmode(STDERR, "utf8");



#
# Subroutines
#
sub get_code_toggle {

    my ($id, $content) = @_;

    return div({-class => "formulaSingleForm"},
               h6("(".a({-class => "toggle", -href => "#$id"},
                        "show/hide code").")")
              .div({-class => "hideable"},
                   $content));

}

#
# Main
#
my $input_file;
unless ((scalar @ARGV) == 1 and -f $ARGV[0]) {
    print STDERR "Invalid parameters.\n\n";
    print STDERR "Usage: \n";
    print STDERR "\t$0 <mias-indexing-output-file>\n";
    print STDERR "Example:\n";
    print STDERR "\tjava -jar MIaS.jar -conf mias.properties |& tee mias-indexing.log\n";
    print STDERR "\t$0 mias-indexing.log > mias-index-report.xhtml\n";
    exit 1;
} else {
    $input_file = $ARGV[0];
}

my @input_lines = path($input_file)->lines_utf8;

# Reading state
# 0 - out of scope of our interests
# 1 - mterm
# 2 - xml prologue
# 3 - xml formula line
my $state = 0;
my $formula_no = 0;
my $data;
while (my $l = shift @input_lines) {

    if ($state == 1) {
        $data->{$formula_no}->{'mterm'} = '<unknown>';
        $data->{$formula_no}->{'rank'} = '<unknown>';
        my ($null, $mterm, $rank) = split(/\s+/, $l);
        $data->{$formula_no}->{'mterm'} = $mterm;
        $data->{$formula_no}->{'rank'} = $rank;
        $state = 2;
        next;
    } elsif ($state == 2) {
        $state = 3;
        next;
    } elsif ($state == 3) {
        if ($l =~ /^$/) {
            $state = 0;
        } else {
            $data->{$formula_no}->{'xml'} .= $l;
        }
        next;
    }

    if ($l =~ /^### Formula no\. \d+ ###$/) {
        $state = 1;
        $formula_no++;
    }

}

print q{<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="cs">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <title>MIaS Index Report</title>
    <style type="text/css">
    h6 a {
        color: gray;
    }
    mi {
        background-color: #cce5ff;
    }
    mo {
        background-color: #e6ffcc;
    }
    mn {
        background-color: #ffffcc;
    }
    </style>
    <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/2.2.0/jquery.min.js"></script>
    <script type="text/javascript">
    $(document).ready(function() {

        $(".hideable").hide();

        $(".hideAll").click(function(){
          $(".hideable").hide("fast");
        });
        $(".showAll").click(function(){
          $(".hideable").show("fast");
        });

        $(".showFormula").click(function(){
          $(this).parents(".formula").find(".hideable").show("fast");
        });
        $(".hideFormula").click(function(){
          $(this).parents(".formula").find(".hideable").hide("fast");
        });

        $(".toggle").click(function(){
          $(this).parents(".formulaSingleForm").find(".hideable").toggle("fast");
        });

    });
    </script>
</head>
<body>
<h1>MIaS Index Report</h1>
};

foreach my $id (sort { $data->{$b}->{'rank'} <=> $data->{$a}->{'rank'} } keys %$data) {
    printf("<h2>Rank %0.10f (formula %d)</h2>\n", escapeHTML($data->{$id}->{'rank'}), $id);
    printf("<p>MTerm: <code>%s</code></p>\n", CGI::escapeHTML($data->{$id}->{'mterm'}));
    printf("%s%s%s\n", q{<div style="font-size: 200%; background-color: #f2f2f2;"><math xmlns="http://www.w3.org/1998/Math/MathML">}, $data->{$id}->{'xml'}, q{</math></div>});
    print get_code_toggle("$id", sprintf("<pre>%s</pre>\n", CGI::escapeHTML($data->{$id}->{'xml'})));
}

print q{</body>
</html>
};
