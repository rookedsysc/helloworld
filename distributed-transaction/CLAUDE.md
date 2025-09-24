## ✅ **1. SOLID Principles**

Adhere strictly to the SOLID principles:

| Principle             | Guideline                                                                                                            | Purpose                           |
| --------------------- | -------------------------------------------------------------------------------------------------------------------- | --------------------------------- |
| Single Responsibility | Every class/function should have **one responsibility**. Immediately split if responsibilities expand.               | Minimize impact of changes        |
| Open/Closed           | Code should be open for **extension** but closed to **modification**. Use interfaces, abstractions, and composition. | Maintain code stability           |
| Liskov Substitution   | Derived classes must be **fully substitutable** for base classes/interfaces without side effects or exceptions.      | Reliable inheritance hierarchy    |
| Interface Segregation | Prefer **small, specific interfaces** over large generic ones. Avoid forcing clients into unnecessary dependencies.  | Reduce coupling                   |
| Dependency Inversion  | Depend on **abstractions**, not on concrete implementations.                                                         | Improve flexibility & testability |

⚠️ **Important:**

- If the code complexity grows or responsibilities blur, consider it a SOLID violation and **refactor immediately**.
- Always comment on which SOLID principle you applied and why.

---

## 🎯 **2. Design Patterns**

Apply design patterns when they meaningfully improve readability, extensibility, or separation of concerns. Do not introduce patterns unnecessarily.

| Situation                      | Applicable Patterns              | Conditions / Purpose                                 |
| ------------------------------ | -------------------------------- | ---------------------------------------------------- |
| Object Creation & Management   | Factory Method, Abstract Factory | Encapsulate object creation logic                    |
| Complex Object Configuration   | Builder                          | Complex objects requiring step-by-step creation      |
| Global Shared Instances        | Singleton (**use sparingly**)    | Only for necessary global state management           |
| Algorithm/Logic Variations     | Strategy                         | Swappable algorithms (payments, auth methods)        |
| Behavior Dependent on State    | State                            | Objects frequently changing internal behavior        |
| Encapsulate Operations         | Command                          | Undo, redo, logging, task queuing                    |
| Inter-Object Communication     | Observer, Mediator               | Reduce coupling, implement pub/sub                   |
| Structure & Concern Separation | MVC, MVVM, Component-based       | Clearly separate responsibilities in complex systems |

⚠️ **Important:**

- **Composition** is always preferred over inheritance, unless clearly justified.
- Comment explicitly when using a pattern, explaining the choice clearly.
- Avoid adding complexity through unnecessary abstraction or patterns.

---

## 🧹 **3. Refactoring Checklist**

Always check these points during refactoring and mention improvements explicitly:

- [ ] Adheres to SOLID principles?
- [ ] Single clear responsibility?
- [ ] Improved naming or unclear logic?
- [ ] Design patterns used effectively?
- [ ] Abstraction clear and dependencies explicit?

---

## 🚨 **4. Anti-Patterns (Forbidden Practices)**

Avoid the following and refactor immediately upon identification:

- ❌ **God classes/functions** (multiple responsibilities)
- ❌ Empty methods to avoid forced interface implementation
- ❌ Deep and unnecessary inheritance chains
- ❌ Excessive global variables or Singleton abuse
- ❌ Over-complexity due to excessive pattern usage

---

## 🌟 **5. Additional Recommended Practices**

- **YAGNI (You Aren’t Gonna Need It)**: Avoid implementing unnecessary features prematurely; provide extensible structures instead.
- **DRY (Don't Repeat Yourself)**: Immediately abstract repeated code into separate modules or methods.
- Provide clear, SOLID-based feedback during code reviews to continually improve quality.


---

## 🎖️ **Ultimate Goal**

The ultimate goal of these guidelines is:

> **“Code that everyone can easily understand, maintain, and confidently modify.”**

Consistently revisit and refine these guidelines to continually enhance code quality.
