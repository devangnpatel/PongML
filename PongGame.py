"""
Pong Game - Deep RL with Python: Keras and SciKitLearn
"""

import random
import numpy as np

max_num_matches = 25000  # number of matches to play during training session
num_units       = 150   # number of units to use in Neural Network
gamma           = 0.99  # discount factor to use in rewards function
        
game_width      = 125 # width of game screen  [java: 320]
game_height     = 100 # height of game screen [java: 240]
paddle_height   = 15 # height of paddle      [java: 39]
paddle_ball_refresh_ratio = 5 # paddle moves this many times per one ball movement
paddle_dy       = 1  # incremental pixel distance to move paddle per time-step
max_spin_inc    = 4  # max spin that can be applied to ball
                     # based on where the ball hits the paddle
                     # further towards the end of the paddle, spin_inc = max_spin_inc
                     # further towards the center of the paddle, spin_inc = 0
                     # dy is increased or decreased by the spin_inc

min_dx = 2  # minimum possible dx
max_dx = 6  # maximum possible dx
min_dy = 2  # minimum possible dy
max_dy = 6  # maximum possible dy

winning_score            = 21 # maximum score: first to this score wins the match
num_volleys_before_start = 0  # min num volleys before official start of game
num_matches_to_record    = 10 # number of evenly-spaced matches to record for replay

# variables to store pixel data of game history
#       pixels: width * height * numberHistoryFrames
#    positions: ball_x, ball_y, paddle_1_y, paddle_2_y
# RIGHT NOW, updating these 2 arrays has num_history_frames hardcoded to 2 [2/12/2020]
num_history_frames = 2  # number of most recent frames to keep for training
num_pixels         = game_width*game_height
game_pixels        = [ [0] * num_pixels for _ in range(num_history_frames)]
game_positions     = [ [0] * 4 for _ in range(num_history_frames)]

# simple static variables
divider_width  = 3	# width of center dividing line
ball_width     = 3	# width of ball
ball_height    = 3	# height of ball
paddle_width   = 3	# width of paddle

# initial values for dynamic changing variables
start_x            = int(game_width/2)   # ball x starting position
start_y            = int(game_height/2)  # ball y starting position
start_dx           = min_dx + int((max_dx-min_dx)/2) # ball starting x speed
start_dy           = min_dy + int((max_dy-min_dy)/2) # ball starting y speed
player_one_start_y = int(game_height/2) # player one paddle starting center position
player_two_start_y = int(game_height/2) # player two paddle starting center position

# global state variables
game_over        = False
num_volleys      = 0 # keeps track of number of volleys that occur in a game
player_one_score = 0 # keeps track of number of games player 1 has won
player_two_score = 0 # keeps track of number of games player 2 has won

# dynamic changing variables
ball_x       = start_x            # ball x position
ball_y       = start_y            # ball y position
ball_dx      = start_dx           # ball x speed
ball_dy      = start_dy           # ball y speed
player_one_y = player_one_start_y # player one paddle center position
player_two_y = player_two_start_y # player two paddle center position

"""
startNewGame
- initializes variables, sizes, dimensions and velocities for a new game
"""
def start_new_game():
	#initialize game state variables
	global ball_x
	global ball_y
	global ball_dx
	global ball_dy
	global player_one_y
	global player_two_y
	global num_volleys
	global game_positions
	global game_pixels
	global game_over

	game_over = False
	num_volleys = 0

	ball_x = start_x
	ball_y = start_y
	ball_dx = start_dx
	ball_dy = start_dy
	player_one_y = player_one_start_y
	player_two_y = player_two_start_y

	# initialize training set of most recent frames to all zeroes
	game_pixels = [ [0] * num_pixels for _ in range(num_history_frames)]
	game_positions = [ [0] * 4 for _ in range(num_history_frames)]

	# initialize speed of ball at serving
	ball_dx = int(min_dx + (max_dx-min_dx)/2)
	ball_dy = int(min_dy + random.random()*(max_dy-min_dy))

	# initialize left-right direction of ball at serving
	if ((random.random()-0.5) <= 0.0):
		ball_dx = -1*abs(ball_dx)
	else:
		ball_dx = abs(ball_dx)
	
	# initialize up-down direction of ball at serving
	if ((random.random()-0.5) <= 0.0):
		ball_dy = -1*abs(ball_dy)
	else:
		ball_dy = abs(ball_dy)

"""
is_game_over: checks if a player has scored [is ball_x beyond left or right of window]
 - increments a player's score if game is over
 - also checks if minimum number of volleys before game start has occurred
 return: True if a player has scored, False if ball is still in play
"""
def is_game_over():
	global game_over
	global player_one_score
	global player_two_score

	# check for at least 3 volleys [if this requirement is set]
	if num_volleys < num_volleys_before_start:
		if (ball_x > game_width) or (ball_x < 0):
			start_new_game()
			return False

	# did player one score?
	if ball_x > game_width:
		game_over = True
		player_one_score+=1
		print("player one wins game")

	# did player two score?
	if ball_x < 0:
		game_over = True
		player_two_score+=1
		print("player two wins game")

	return game_over
    
"""
is_match_over: checks if match is over [is score greater than max = 21]
 return: True if match is over, False if match is not over
"""
def is_match_over():
	if (player_one_score >= winning_score) or (player_two_score >= winning_score):
		return True
	else:
		return False

"""
move_player_one_paddle: moves paddle [negative up, positive down]
 input py: the number of pixels to move the paddle
"""
def move_player_one_paddle(py):
	global player_one_y

	player_one_y += py
	if (player_one_y - paddle_height/2) <= 0:
		player_one_y = int(paddle_height/2)
	if (player_one_y + paddle_height/2) >= game_height:
		player_one_y = int(game_height - paddle_height/2)

"""
move_player_two_paddle: moves paddle [negative up, positive down]
 input py: the number of pixels to move the paddle
"""
def move_player_two_paddle(py):
	global player_two_y

	player_two_y += py
	if ((player_two_y - paddle_height/2) <= 0):
		player_two_y = int(paddle_height/2)
	if ((player_two_y + paddle_height/2) >= game_height):
		player_two_y = int(game_height - paddle_height/2)

"""
player_one_CPU_move_paddle: simply follows y-position of ball
"""
def player_one_CPU_move_paddle():
	if ball_dx < 0:
		predicted_y = ball_y
		move_multiplier = paddle_dy*paddle_ball_refresh_ratio
		# move paddle up...
		if player_one_y >= predicted_y:
			move_player_one_paddle(-1*move_multiplier)

		# move paddle down...
		elif player_one_y < predicted_y:
			move_player_one_paddle(move_multiplier)

"""
Player 2 CPU Algorithm: simply follows y-position of ball
-not used-
"""
def player_two_CPU_move_paddle():
	if ball_dx > 0:
		predicted_y = ball_y
		move_multiplier = paddle_dy*paddle_ball_refresh_ratio
        # move paddle up...
		if player_two_y >= predicted_y:
			move_player_two_paddle(-1*move_multiplier)
			return 1

		# move paddle down...
		elif player_two_y < predicted_y:
			move_player_two_paddle(move_multiplier)
			return 0
	return 0

"""
update_ball: moves the ball one unit according to dx, dy
 - moves ball one screen-refresh unit (dx,dy)
 - checks for collision at top, bottom and reverses direction accordingly
 - checks for collision with paddle, and reverses direction accordingly
 - if there is a collision with a paddle, the y-direction velocity(dy)
   is adjusted according to the region of the paddle that the ball hits
 - When the ball hits the paddle, the y-speed is increased proportional
   to the distance from the center of the paddle if the ball hits
   the far side of the paddle
 - the y-speed is decreased proportional to the distance from the center
   of the paddle if the ball hits the near side of the paddle
"""
def update_ball():
	global ball_x
	global ball_y
	global ball_dx
	global ball_dy
	global num_volleys

	# update position
	ball_x += ball_dx
	ball_y += ball_dy

	# check for collision at bottom
	if ball_y >= (game_height - 1):
		ball_y = game_height-1
		ball_dy = -1*abs(ball_dy)

	# check for collision at top
	if ball_y <= 0:
		ball_y = 0
		ball_dy = abs(ball_dy)
        
	# check for collision with left paddle (player 1)
	if (ball_x <= 0) and (ball_y >= (player_one_y - paddle_height/2)) and (ball_y <= (player_one_y + paddle_height/2)):
		# calculate an offset that adjusts spin of ball based on region of paddle at collision
		offset = 0

        # going up . . .
		if ball_dy <= 0:
			offset = int(max_spin_inc*(player_one_y - ball_y)/(paddle_height/2))
		elif ball_dy > 0:
			offset = int(max_spin_inc*(ball_y-player_one_y)/(paddle_height/2))

		if ball_dy <= 0:
			ball_dy += offset
			if ball_dy <= (-1*max_dy):
				ball_dy = -1*max_dy
			if ball_dy >= (-1*min_dy):
				ball_dy = -1*min_dy
		elif (ball_dy > 0):
			ball_dy += offset
			if ball_dy <= min_dy:
				ball_dy = min_dy
			if ball_dy >= max_dy:
				ball_dy = max_dy

		ball_x = 1
		ball_dx = abs(ball_dx)
		num_volleys+=1

	# check for collision with right paddle (player 2)
	if (ball_x >= (game_width - 1)) and (ball_y >= (player_two_y - paddle_height/2)) and (ball_y <= (player_two_y + paddle_height/2)):
		# calculate an offset that adjusts spin of ball depending on location of collision
		offset = 0

		if ball_dy <= 0:
			offset = int(max_spin_inc*(player_two_y - ball_y)/(paddle_height/2))
		elif ball_dy > 0:
			offset = int(max_spin_inc*(ball_y-player_two_y)/(paddle_height/2))

		if ball_dy <= 0:
			ball_dy += offset
			if ball_dy <= (-1*max_dy):
				ball_dy = -1*max_dy
			if ball_dy >= (-1*min_dy):
				ball_dy = -1*min_dy
		elif ball_dy > 0:
			ball_dy += offset
			if ball_dy <= min_dy:
				ball_dy = min_dy
			if ball_dy >= max_dy:
				ball_dy = max_dy

		ball_x = game_width - 1
		ball_dx = -1*abs(ball_dx)
		num_volleys+=1
        
	ball_x  = int(ball_x)
	ball_y  = int(ball_y)
	ball_dx = int(ball_dx)
	ball_dy = int(ball_dy)

def get_position_array():
	# global position_array

	# returns the position parameters of the most recent frame
	# - only the 4 changable parameters:
	#   {x,y,paddle1-y,paddle2-y}
	position_array = [ball_x,ball_y,player_one_y,player_two_y]
	return position_array

def get_pixel_array():
	# global pixel_array

	# returns thes rasterized pixel values of the most recent frame
	# - the game is monochrome, so a pixel is:
	#   1 for the ball or the paddles, and
	#   0 for everything else like the black background
	pixel_array = [0] * num_pixels
	# for h in range(height):
	#	for w in range(width):
	#		pixelArray[width*h + w] = 0

	pixel_array[game_width*ball_y + ball_x] = 1
	for p in range(int(max(0, player_one_y - paddle_height/2)), int(min(game_height, player_one_y + paddle_height/2)),1):
		pixel_array[game_width*p] = 1
	for p in range(int(max(0, player_two_y - paddle_height/2)), int(min(game_height, player_two_y + paddle_height/2)),1):
		pixel_array[game_width*p + game_width-1] = 1

	return pixel_array

def get_mirror_array(original_array, p_width, p_height):
	# returns an array of the game frame pixels, but mirrored
	# - this is used to swap the side of a paddle, so that
	#   a trained ML model can be swapped to play from the left
	#   side instead of the right-side
	p_size = p_width*p_height
	new_array = [0]*p_size
	for h in range(p_height):
		for w in range(p_width):
			new_array[p_width*h + (p_width-1)-w] = original_array[p_width*h + w]
	return new_array

"""
updateGamePixelFrames: updates the most recent pixel values per game frame
 - These frames are the x-training input into an ML-model
 - This method puts the most recent frame's values at the top, and
   knocks off the oldest frame
"""
def update_game_pixel_frames():
	global game_pixels
	# numFrames = numHistoryFrames
	# for p in range(numFrames-1,0,-1):
	# 	gamePixels[p] = gamePixels[p-1]
	game_pixels[1] = game_pixels[0]
	game_pixels[0] = get_pixel_array()


"""
updateGamePositionFrames: updates the most recent position values per game frame
 - This method puts the most recent frame's values at the top, and
   knocks off the oldest frame
 - Training starts with 4 frames (as in the private-int: numHistoryFrames)
 - the position values are the x,y values of the ball and y-values of the paddles
 - when most people train an ML model for a pong game, the exact pixel
   values of the game are used, after a Convolution filter, to train an
   ML model / Neural Network.
 - Here, I use the pixel values (and not images) to make the training,
   quicker. And then I compare that model with a model that
   does not use the pixel values, but rather the only properties of the
   the game that change: the ball position (x,y) and the paddle positions(y)
 - I compare the accuracy and training-time of a model using pixel values
   and a model using these 4 position values.
"""
def update_game_position_frames():
	global game_positions
	# numFrames = numHistoryFrames
	# for p in range(numFrames-1,0,-1):
	#	gamePositions[p] = gamePositions[p-1]
	game_positions[1] = game_positions[0]
	game_positions[0] = get_position_array()


"""
Save every frame into an array
Then output that array to a txt file for downloading
And replay from a Java display
"""
game_data = []
def record_game_data():
	game_data.append(int(ball_x))
	game_data.append(int(ball_y))
	game_data.append(int(player_one_y))
	game_data.append(int(player_two_y))

def output_game_data_to_txt_file(filename):
	header_str = str(game_width) + " " + str(game_height) + " " + str(paddle_height) + " " + str(len(game_data))
	np.savetxt(filename,game_data,fmt='%i',delimiter=' ',header=header_str)

"""
start_simulated_match
 - Main Match/Games Loop for a match, and ends when winning score is reached
 - Both players are simulated by CPU with a simple algorithm
"""
def start_simulated_match():
	global player_one_score
	global player_two_score

	player_one_score = 0
	player_two_score = 0

	while not is_match_over():
		start_new_game()
		while not is_game_over():
			if num_volleys >= num_volleys_before_start:
				update_game_pixel_frames()
				update_game_position_frames()
			record_game_data()
			update_ball()
			for m in range(5):
				player_one_CPU_move_paddle()
				player_two_CPU_move_paddle()
	if player_one_score >= winning_score:
		print("Player 1 wins match")
	if (player_two_score >= winning_score):
		print("Player 2 wins match")


"""
Reinforcement Learning - Neural Network
"""
from keras.layers import Dense
from keras.models import Sequential

d_input = num_pixels

# a common neural network: sequential, 1 hidden layer of 200 units
# standard use of relu activation and sigmoid activation on output
# read: kernel_initializers: RandomNormal, glorot_uniform, Adam optimizer
# use:  accuracy metric, cross-entropy loss function
model = Sequential()
model.add(Dense(units=num_units,input_dim=d_input,activation='relu',kernel_initializer='glorot_uniform'))
model.add(Dense(units=1,activation='sigmoid',kernel_initializer='RandomNormal'))
model.compile(loss='binary_crossentropy',optimizer='adam',metrics=['accuracy'])

x_train = []
y_train = []
rewards = []

# reward discount used by Karpathy (cf. https://gist.github.com/karpathy/a4166c7fe253700972fcbc77e4ea32c5)
def discount_rewards(r, gamma):
  """ take 1D float array of rewards and compute discounted reward """
  r = np.array(r)
  discounted_r = np.zeros_like(r)
  running_add = 0
  # we go from last reward to first one so we don't have to do exponentiations
  for t in reversed(range(0, r.size)):
    if r[t] != 0: running_add = 0 # if the game ended (in Pong), reset the reward sum
    running_add = running_add * gamma + r[t] # the point here is to use Horner's method to compute those rewards efficiently
    discounted_r[t] = running_add
  discounted_r -= np.mean(discounted_r) #normalizing the result
  discounted_r /= np.std(discounted_r) #idem
  return discounted_r

def train_model():
	model.fit(x=np.vstack(x_train),y=np.vstack(y_train),verbose=1,sample_weight=discount_rewards(rewards,gamma))
	reset_training_data()

def reset_training_data():
	global x_train
	global y_train
	global rewards
	x_train = []
	y_train = []
	rewards = []

def create_x_record():
	return np.subtract(game_pixels[0],game_pixels[1])

def update_training_set(xInput,yInput,rInput):
	x_train.append(xInput)
	y_train.append(yInput)
	rewards.append(rInput)

def player_two_ML_move_paddle(xInput):
	response = model.predict(np.expand_dims(xInput,axis=1).T)
	# response = model.predict_on_batch(np.expand_dims(xRecord,axis=1).T)
	# print("response: " + str(response))
	move_multiplier = paddle_dy*paddle_ball_refresh_ratio
	# if response > 0.5:
	# if response > np.random.uniform():
	if response > random.random():
		move_player_two_paddle(-1*move_multiplier)
		# MOVE_UP: 1
		return 1
	else:
		move_player_two_paddle(move_multiplier)
		# MOVE_DOWN: 0
		return 0


import time

"""
start_training_match
 - Trains the Deep RL Neural Network
 - Main Match/Games Loop for a match, and ends when winning score is reached
 - Both players are simulated by CPU with a simple algorithm
"""
def start_training_match():
	global player_one_score
	global player_two_score

	player_one_score = 0
	player_two_score = 0

	match_counter = 0
	mod_match_counter = int(max_num_matches/num_matches_to_record)
	
	sTime = time.perf_counter()

	while (player_one_score < winning_score) and (player_two_score < winning_score) and (match_counter <= max_num_matches):
		start_new_game()
		while (ball_x > 0) and (ball_x < game_width):
			# update frame history
			update_game_pixel_frames()
			update_game_position_frames()
			x_record = create_x_record()

			# move player 1 paddle
			player_one_CPU_move_paddle()
            
			# move player 2 paddle based on current frame state: returns 1 on MOVE_UP and 0 on MOVE_DOWN
			# y_response = 0
			# if match_counter <= 0:
			#	y_response = player_two_CPU_move_paddle()
			# else:
			# 	y_response = player_two_ML_move_paddle(x_record)
			y_response = player_two_ML_move_paddle(x_record)
            
			# move ball
			update_ball()

			if (match_counter % mod_match_counter) == 0:
				record_game_data()

			if (ball_x > 0) and (ball_x < game_width):
				update_training_set(x_record,y_response,0.0)
			elif ball_x <= 0:
				update_training_set(x_record,y_response,1.0)
				player_two_score+=1
				print("*****************************")
				print("************** Player 2 wins game: " + str(player_one_score) + " " + str(player_two_score) + "  volleys: " + str(num_volleys))
				dTime = time.perf_counter() - sTime
				print("game took: " + str(dTime) + " seconds")
				sTime = time.perf_counter()
			elif ball_x >= game_width:
				update_training_set(x_record,y_response,-1.0)
				player_one_score+=1
				print("*****************************")
				print("************** Player 1 wins game: " + str(player_one_score) + " " + str(player_two_score) + "  volleys: " + str(num_volleys))
				dTime = time.perf_counter() - sTime
				print("game took: " + str(dTime) + " seconds")
				sTime = time.perf_counter()

		if player_one_score >= winning_score:
			print("*****************************")
			print("************** Player 1 wins match(" + str(match_counter) + "): " + str(player_one_score-player_two_score))
			player_one_score = 0
			player_two_score = 0
			match_counter+=1
			train_model()
		elif (player_two_score >= winning_score):
			print("*****************************")
			print("************** Player 2 wins match(" + str(match_counter) + "): " + str(player_one_score-player_two_score))
			player_one_score = 0
			player_two_score = 0
			match_counter+=1
			train_model()
			

start_training_match()
output_game_data_to_txt_file("TrainingOutput.txt")
