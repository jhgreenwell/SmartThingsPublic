/**
 *  My First SmartApp
 *
 *  Copyright 2018 JAMES GREENWELL
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
definition(
    name: "My First SmartApp",
    namespace: "jhgreenwell",
    author: "JAMES GREENWELL",
    description: "Turn on nightlight based on motion",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	section("Turn on when motion is detected:") {
		input "themotion", "capability.motionSensor", required: true, title: "Where?"
    }
    section("Turn off when there's been no movement for"){
    	input "minutes", "number", required: true, title: "Minutes?"
    }
    section("Turn on this light"){
    	input "theswitch", "capability.switch", required: true
    }
    section("Set Light Level"){
    	input "lightlevel", "number", range: "0..100", defaultValue: "100", title: "Brightness", description: "Only for compatible lights"
    }
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def initialize() {
	subscribe(themotion, "motion.active", motionDetectedHandler)
    subscribe(themotion, "motion.inactive", motionStoppedHandler)
}

def motionDetectedHandler(evt){
	log.debug "motionDetectedHandler called: $evt"
    
    def hasSwitchLevel = theswitch.hasCapability("Switch Level")
    log.debug "${theswitch.displayName} has Switch Level capability? $hasSwitchLevel"
    
    if(hasSwitchLevel){
    	log.debug "Setting light level to $lightlevel"
    	theswitch.setLevel(lightlevel)
    }else{    
    	theswitch.on()
    }
}

def motionStoppedHandler(evt){
	log.debug "motionStoppedHandler called: $evt"
    
    runIn(60*minutes, checkMotion)
}

def checkMotion(){
	log.debug "In checkMotion scheduled method"
    
    def motionState = themotion.currentState("motion")
    
    if(motionState.value == "inactive"){
    	def elapsed = now() - motionState.date.time
        
        def threshold = 1000*60*minutes
        
        if(elapsed >= threshold){
        	log.debug "Motion has stayed inactive long since last check ($elapsed ms); turning switch off"
            theswitch.off()
        }else{
        	log.debug "Motion has not stayed inactive long enough since last check ($elapsed ms); do nothing"
        }
    }else{
    	log.debug "Motion is active, do nothing and wait for inactive"
    }
}