//
//  DeliveryViewController.swift
//  UAS Delivery
//
//  Created by Kirill Kultinov on 1/31/17.
//  Copyright © 2017 Kirill Kultinov. All rights reserved.
//

import UIKit
import CoreData
import FirebaseAuth

class DeliveryViewController: UIViewController, UITextFieldDelegate {


    @IBOutlet weak var passwordTextField: UITextField!
    @IBOutlet weak var loginTextField: UITextField!
    @IBOutlet weak var menuButton: UIBarButtonItem!
    
    var loginState = [NSManagedObject]()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        //Looks for single or multiple taps.
        let tap: UITapGestureRecognizer = UITapGestureRecognizer(target: self, action: "dismissKeyboard")
        
        //allow opening the menu
        menuButton.target = revealViewController()
        menuButton.action = #selector(SWRevealViewController.revealToggle(_:))
        print("did load delivery")
        
        
        //allow swiping in order to open/close the menu
        if self.revealViewController() != nil {
            
            self.view.addGestureRecognizer(self.revealViewController().panGestureRecognizer())
            self.view.addGestureRecognizer(self.revealViewController().tapGestureRecognizer())
        }

        // Do any additional setup after loading the view.
        view.addGestureRecognizer(tap)
        self.passwordTextField.delegate = self
        self.loginTextField.delegate = self
        //delete current data from the CoreData
        fetchState()
        deleteState()
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    @IBAction func loginBtnPressed(_ sender: Any) {

        ////implement login logic
        if loginTextField.text != nil && passwordTextField.text != nil{
            FIRAuth.auth()?.signIn(withEmail: loginTextField.text!, password: passwordTextField.text!, completion: { (user, error) in
                if let u = user{
                    UserDefaults().set(self.loginTextField.text, forKey: "login")
                    UserDefaults().set(self.passwordTextField.text, forKey: "password")
                    print("loged in successfully")
                    self.performSegue(withIdentifier: "loginProcess", sender: self)
                }
                else{
                    let alertController = UIAlertController(title: "Error!", message:
                        "Entered login and/or password is invalid. Try again.", preferredStyle: UIAlertControllerStyle.alert)
                    alertController.addAction(UIAlertAction(title: "OK", style: UIAlertActionStyle.destructive,handler: nil))
                    
                    self.present(alertController, animated: true, completion: nil)
                }
            })
        }

        

    }
    
    func dismissKeyboard() {
        //Causes the view (or one of its embedded text fields) to resign the first responder status.
        view.endEditing(true)
    }
    
    func textFieldShouldReturn(_ textField: UITextField) -> Bool {
        self.view.endEditing(true)
        return false
    }
    
    //remove the login state
    func deleteState(){
        // remove the deleted item from the model
        let appDelegate = UIApplication.shared.delegate as! AppDelegate
        let managedContext = appDelegate.managedObjectContext
        
        if loginState.isEmpty {
            print("login state is now empty")
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

    /*
    // MARK: - Navigation

    // In a storyboard-based application, you will often want to do a little preparation before navigation
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        // Get the new view controller using segue.destinationViewController.
        // Pass the selected object to the new view controller.
    }
    */

}
