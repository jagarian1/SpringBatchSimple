

@RestController
@RequestMapping("/users")  // 실제 URL은 /api/v1/users
public class UserController {
    
    @GetMapping
    public List<User> getUsers() {
        // URL: GET /api/v1/users
        return userService.findAll();
    }
    
    @PostMapping
    public User createUser(@RequestBody User user) {
        // URL: POST /api/v1/users
        return userService.create(user);
    }
    
    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id) {
        // URL: GET /api/v1/users/{id}
        return userService.findById(id);
    }
}