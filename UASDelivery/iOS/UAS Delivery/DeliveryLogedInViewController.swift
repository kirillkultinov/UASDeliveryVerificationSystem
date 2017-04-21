//
//  DeliveryLogedInViewController.swift
//  UAS Delivery
//
//  Created by Kirill Kultinov on 3/3/17.
//  Copyright © 2017 Kirill Kultinov. All rights reserved.
//

import UIKit
import CoreData
import Firebase
import FirebaseAuth

class DeliveryLogedInViewController: UIViewController {
    @IBOutlet weak var menuButton: UIBarButtonItem!

    var loginState = [NSManagedObject]()
    
    @IBOutlet weak var deliveryStatus: UILabel!
    
    @IBOutlet weak var verificationCode: UILabel!
    
    @IBOutlet weak var droneName: UILabel!
    
    @IBOutlet weak var verifyButton: UIButton!
    
    @IBOutlet weak var pairButton: UIButton!
    let reference = FIRDatabase.database().reference()
    let userID = FIRAuth.auth()?.currentUser?.uid
    
    
    var timerTXDelay: Timer?
    var allowTX = true
    
    override func viewDidLoad() {
        super.viewDidLoad()
        //allow opening the menu
        menuButton.target = revealViewController()
        menuButton.action = #selector(SWRevealViewController.revealToggle(_:))
        print("did load delivery loged in")
        
        let login = UserDefaults().string(forKey: "login") ?? ""
        let password = UserDefaults().string(forKey: "password") ?? ""
        
        print(login)
        print(password)
        
        //allow swiping in order to open/close the menu
        if self.revealViewController() != nil {
            
            self.view.addGestureRecognizer(self.revealViewController().panGestureRecognizer())
            self.view.addGestureRecognizer(self.revealViewController().tapGestureRecognizer())
        }
        //hide disable all necessary design elements
        self.verificationCode.isHidden = true
        self.droneName.isHidden = true
        verifyButton.isEnabled = false
        verifyButton.backgroundColor = UIColor.gray
        pairButton.isEnabled = false
        pairButton.backgroundColor = UIColor.gray
        

        // Do any additional setup after loading the view.
        writeState()
        updateStatus()
        
        
        // Watch Bluetooth connection
        NotificationCenter.default.addObserver(self, selector: #selector(DeliveryLogedInViewController.connectionChanged(_:)), name: NSNotification.Name(rawValue: BLEServiceChangedStatusNotification), object: nil)
    }
    
    deinit {
        NotificationCenter.default.removeObserver(self, name: NSNotification.Name(rawValue: BLEServiceChangedStatusNotification), object: nil)
    }
    
    override func viewDidAppear(_ animated: Bool) {
        
    }
    

    
    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        
        self.stopTimerTXDelay()
    }
    
    

    
    //save a logedIn state to CoreData
    func writeState(){
        
        if let appDelegate = UIApplication.shared.delegate as? AppDelegate {

            
            fetchState()
            
            if(loginState.isEmpty){
                let managedContext = appDelegate.managedObjectContext
                let entity =  NSEntityDescription.entity(forEntityName: "LoginState", in:managedContext)
                let state = NSManagedObject(entity: entity!, insertInto: managedContext)
                state.setValue(true, forKey: "logedIn")
                print("wrote to the coredata the login state")
                
                do {
                    try managedContext.save()
                } catch let error as NSError  {
                    print("Could not save \(error), \(error.userInfo)")
                }
            }

        }
        
    }
    
    func fetchState(){
        //1
        let appDelegate = UIApplication.shared.delegate as! AppDelegate
        
        let managedContext = appDelegate.managedObjectContext
        
        //2
        let fetchRequest = NSFetchRequest<NSFetchRequestResult>(entityName: "LoginState")
        
        //3
        do {
            let result = try managedContext.fetch(fetchRequest)
            loginState = result as! [NSManagedObject]
            print(loginState.count)
            if(!loginState.isEmpty){
                print("fetch state is not empty. Fetched")
            }
            else{
                print("fetch state is empty. Fetched")
            }
            
        } catch let error as NSError {
            print("Could not fetch \(error), \(error.userInfo)")
        }
    }
    
    //remove the login state
    func deleteState(){
        // remove the deleted item from the model
        let appDelegate = UIApplication.shared.delegate as! AppDelegate
        let managedContext = appDelegate.managedObjectContext
        
        if loginState.isEmpty {
            print("empty")
        }
        else{
            let i = 0
            while(i < loginState.count){
                let commit = loginState[i]
                managedContext.delete(commit)
                loginState.remove(at: i)
                print("deleted")
            }
            do {
                try managedContext.save()
            } catch let error as NSError {
                print("Could not delete \(error), \(error.userInfo)")
            }
            print("after deleting: ",  (loginState.count))
            
        }
    }
    
    
    func updateStatus(){
        let userReference = reference.child(byAppendingPath: userID!)
        
        userReference.observe(FIRDataEventType.value, with: { (snapshot) in
            let value = snapshot.value as? NSDictionary
            
            let delStat = value?["deliveryStatus"] as? String ?? ""
            let code = value?["verificationCode"] as? String ?? ""
            let drone = value?["droneName"] as? String ?? ""
            self.verificationCode.text = "Verification code: " + String(describing: code)
            self.droneName.text = "Drone's name: " + drone
            
            
            
            if delStat == "being processed"{
                self.deliveryStatus.text = "Your package is being processed"
                self.verificationCode.isHidden = true
                self.droneName.isHidden = true
            }
            else if delStat == "being delivered"{
                self.deliveryStatus.text = "Your package is being delivered"
                self.droneName.isHidden = false
                self.verificationCode.isHidden = false
                self.pairButton.isEnabled = true
                self.pairButton.backgroundColor = UIColor(red: 34.0/255.0, green: 255.0/255.0, blue: 35.0/255.0, alpha: 1.0)
            }
            else if delStat == "has arrived"{
                self.deliveryStatus.text = "Your package has arrived"
                self.droneName.isHidden = false
                self.verificationCode.isHidden = false
                self.pairButton.isEnabled = true
                self.pairButton.backgroundColor = UIColor(red: 34.0/255.0, green: 255.0/255.0, blue: 35.0/255.0, alpha: 1.0)
            }
            else if delStat == "has been delivered"{
                self.deliveryStatus.text = "Your package has been delivered"
                self.droneName.isHidden = false
                self.verificationCode.isHidden = true
                self.pairButton.isEnabled = false
                self.pairButton.backgroundColor = UIColor.gray
                self.verifyButton.isEnabled = false
                self.pairButton.backgroundColor = UIColor.gray
            }
            else{
                self.deliveryStatus.text = "Some error has happened :("
            }
            
            
        })
        
    }
    
    //pair button pressed
    @available(iOS 10.0, *)
    @IBAction func openBluetoothSettings(_ sender: Any) {
        
        // Start the Bluetooth discovery process
        _ = btDiscoverySharedInstance
        print("created btDiscovery")
        
        Timer.scheduledTimer(withTimeInterval: 3, repeats: true) { timer in
            self.verifyButton.isEnabled = true
            self.verifyButton.backgroundColor = UIColor(red: 34.0/255.0, green: 255.0/255.0, blue: 35.0/255.0, alpha: 1.0)
        }
 
    }
    
    //verify button pressed
    @IBAction func verifyButtonPressed(_ sender: Any) {
        self.sendPosition(UInt8(1))
    }
    
    func connectionChanged(_ notification: Notification) {
        // Connection status changed. Indicate on GUI.
        let userInfo = (notification as NSNotification).userInfo as! [String: Bool]
        
        DispatchQueue.main.async(execute: {
            // Set image based on connection status
            if let isConnected: Bool = userInfo["isConnected"] {
                if isConnected {
                    print("connected")
                    
                    // Send current slider position
                    self.sendPosition(UInt8(1))
                } else {
                    print("disconnected")
                }
            }
        });
    }
    
    func sendPosition(_ position: UInt8) {
        // Valid position range: 0 to 180
        
        if !allowTX {
            return
        }
        
        // Send position to BLE Shield (if service exists and is connected)
        if let bleService = btDiscoverySharedInstance.bleService {
            bleService.writePosition(position)
            
            
            // Start delay timer
            allowTX = false
            if timerTXDelay == nil {
                timerTXDelay = Timer.scheduledTimer(timeInterval: 0.1, target: self, selector: #selector(DeliveryLogedInViewController.timerTXDelayElapsed), userInfo: nil, repeats: false)
            }
        }
    }
    
    
    func timerTXDelayElapsed() {
        self.allowTX = true
        self.stopTimerTXDelay()
        
        // Send current slider position
        self.sendPosition(UInt8(1))
    }
    
    func stopTimerTXDelay() {
        if self.timerTXDelay == nil {
            return
        }
        
        timerTXDelay?.invalidate()
        self.timerTXDelay = nil
    }
    
    
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    @IBAction func logoutButtonPressed(_ sender: Any) {
        let firebaseAuth = FIRAuth.auth()
        do {
            try firebaseAuth?.signOut()
        } catch let signOutError as NSError {
            print ("Error signing out: %@", signOutError)
        }
        
        performSegue(withIdentifier: "logoutProcess", sender: self)
        
    }
    
    
    

    /*
    // MARK: - Navigation

    // In a storyboard-based application, you will often want to do a little preparation before navigation
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        // Get the new view controller using segue.destinationViewController.
        // Pass the selected object to the new view controller.
    }
    */

}
