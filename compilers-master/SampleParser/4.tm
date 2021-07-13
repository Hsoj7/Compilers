* C-Minus Compilation to TM Code
* File: 4.tm
* Standard prelude
  0:    LD  6,0(0)  load gp with maxaddress
  1:   LDA  5,0(6)  copy gp to fp
  2:    ST  0,0(0)  clear location 0
* Jump around i/o routines here
* Code for input routine
  4:    ST  0,-1(5)  store return
  5:    IN  0,0,0  input
  6:    LD  7,-1(5)  return to caller
* Code for output routine
  7:    ST  0,-1(5)  store return
  8:    LD  0,-2(5)  load output value
  9:   OUT  0,0,0  output
 10:    LD  7,-1(5)  return to caller
  3:   LDA  7,7(7)  jump around i/o code
* End of standard prelude
* Processing function: main
 12:    ST  0,-1(5)  save return address for main
* -> compound
* processing local var: x
* -> id
* looking up id: x
 13:   LDA  0,-2(5) 	load id x address
* <- id
 14:    ST  0,-3(5) 	op: push left
* Call of function: input
 15:    ST  5,-4(5) 	push ofp
 16:   LDA  5,-4(5) 	push frame
 17:   LDA  0,1(7) 	load ac with return pointer
 18:   LDA  7,-15(7) 	jump to function location
 19:    LD  5,0(5) 	pop frame
* <- call
 20:    LD  1,-3(5) 	load left
 21:    ST  0,0(1) 	store value
* -> If
* if: jump after body comes back here
 22:    LD  0,-2(5) 	load reg 0 with x
 23:    ST  0,-4(5) 	store value of x
* -> constant
 24:   LDC  0,100(0) 	load constant int 100
* <- constant
 25:    ST  0,-5(5) 	store the constant
 26:    LD  1,-4(5) 	op >: load left
 27:   SUB  0,1,0 	subtract for op >
 28:   JLT  0,2(7) 	branch to true
 29:   LDC  0,0(0) 	false case
 30:   LDA  7,1(7) 	unconditional jump
 31:   LDC  0,1(0) 	true case
* if: jump to end belongs here
* -> compound statement
* -> id
* looking up id: x
 33:   LDA  0,-2(5) 	load id x address
* <- id
 34:    ST  0,-6(5) 	op: push left
 35:   LDC  0,100(0) 	load constant 100
 36:    ST  0,-7(5) 	store constant 100
 37:    LD  0,-6(5) 	load variable address
 38:    LD  1,-7(5) 	load register with stored value
 39:    ST  1,0(0) 	store value
* <- compound statement
 40:   LDA  7,-19(7) 	jump to if condition
 32:   JEQ  0,8(7) 	if: jump to end
* <- if
* -> else
* <- else
* Call of function: output
* -> id
* looking up id: x
 41:    LD  0,-2(5) 	load id x
* <- id
 42:    ST  0,-4(5) 	store arg value
 43:    ST  5,-2(5) 	push ofp
 44:   LDA  5,-2(5) 	push frame
 45:   LDA  0,1(7) 	load ac with return pointer
 46:   LDA  7,-40(7) 	jump to function location
 47:    LD  5,0(5) 	pop frame
* <- call
* <- compound
 48:    LD  7,-1(5)  return back to the caller
 11:   LDA  7,37(7)  jump around fn body
* End of execution
 49:    ST  5,0(5) 	push ofp
 50:   LDA  5,0(5) 	push frame
 51:   LDA  0,1(7) 	load ac with return pointer
 52:   LDA  7,-41(7) 	jump to main's location
 53:    LD  5,0(5) 	pop frame
 54:  HALT  0,0,0   
