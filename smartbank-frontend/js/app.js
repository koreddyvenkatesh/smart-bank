const API_BASE_URL = "http://localhost:8081/api";

// STATE

const State = {
    token: localStorage.getItem("token"),
    role: localStorage.getItem("role"),
    username: localStorage.getItem("username"),
    accounts: [],
    resetIdentifier: "",
};

function $(id) {
    return document.getElementById(id);
}


const API = {

    async request(endpoint, options = {}) {

        const headers = {
            "Content-Type": "application/json",
            ...(options.headers || {})
        };

        if (State.token) {
            headers["Authorization"] = `Bearer ${State.token}`;
        }

        const response = await fetch(
            `${API_BASE_URL}${endpoint}`,
            {
                ...options,
                headers
            }
        );

        let data;

        try {
            data = await response.json();
        } catch {
            data = {};
        }

        if (!response.ok) {

            throw new Error(
                data.message ||
                data.error ||
                `Request failed: ${response.status}`
            );
        }

        return data;
    }
};


// TOAST

function showToast(message, type = "success") {

    const toast = $("toast");

    toast.textContent = message;

    toast.classList.add("show");

    if (type === "error") {
        toast.style.background = "#c0392b";
    } else {
        toast.style.background = "#408175";
    }

    setTimeout(() => {
        toast.classList.remove("show");
    }, 3000);
}


// AUTH SECTION SWITCHING

function showLogin() {

    $("loginSection").classList.remove("hidden");
    $("signupSection").classList.add("hidden");
    $("forgotSection").classList.add("hidden");
}


function showSignup() {

    $("loginSection").classList.add("hidden");
    $("signupSection").classList.remove("hidden");
    $("forgotSection").classList.add("hidden");
}


function showForgotPassword() {

    $("loginSection").classList.add("hidden");
    $("signupSection").classList.add("hidden");
    $("forgotSection").classList.remove("hidden");

    $("otpSection").classList.add("hidden");
    $("resetPasswordSection").classList.add("hidden");
}



// LOGIN

async function login(event) {
    event.preventDefault();

    const submitBtn = $("loginForm").querySelector('button[type="submit"]');
    const originalText = submitBtn.textContent;

    const emailOrAccountNumber = $("loginIdentifier").value.trim();
    const password = $("loginPassword").value;


    if (!emailOrAccountNumber || !password) {
        showToast("Please enter your credentials", "error");
        return;
    }


    submitBtn.disabled = true;
    submitBtn.textContent = "Logging in...";

    try {
        const data = await API.request("/auth/login", {
            method: "POST",
            body: JSON.stringify({ emailOrAccountNumber, password })
        });

        if (data.message === "RESET_REQUIRED") {
            State.resetIdentifier = emailOrAccountNumber;
            showToast("Please reset your temporary password", "success");
            showForgotPassword();
            
        
            submitBtn.disabled = false;
            submitBtn.textContent = originalText;
            return;
        }

        State.token = data.token;
        State.role = data.role;
        State.username = emailOrAccountNumber;

        localStorage.setItem("token", data.token);
        localStorage.setItem("role", data.role);
        localStorage.setItem("username", emailOrAccountNumber);

        showToast(data.message || "Login successful", "success");
        
    
        $("loginForm").reset();
        showDashboard(data.role);

    } catch (error) {
        showToast(error.message || "Login failed", "error");
    } finally {
        if (submitBtn) {
            submitBtn.disabled = false;
            submitBtn.textContent = originalText;
        }
    }
}


// DASHBOARD


function showDashboard(role) {

    $("authSection").classList.add("hidden");

    $("dashboardSection").classList.remove("hidden");

    $("customerDashboard").classList.add("hidden");
    $("clerkDashboard").classList.add("hidden");
    $("adminDashboard").classList.add("hidden");

    $("navUser").classList.remove("hidden");


    role = role?.toUpperCase();

    if (role && !role.startsWith("ROLE_")) {
        role = `ROLE_${role}`;
    }


    $("userRole").textContent = role || "USER";
    $("userName").textContent = State.username || "User";


    if (role === "ROLE_CUSTOMER") {

        $("customerDashboard")
            .classList.remove("hidden");

        loadCustomerProfile();

    } else if (role === "ROLE_CLERK") {

        $("clerkDashboard")
            .classList.remove("hidden");

    } else if (role === "ROLE_ADMIN") {

        $("adminDashboard")
            .classList.remove("hidden");

        loadAdminData();

    } else {

        showToast(
            `Unsupported role: ${role}`,
            "error"
        );

        logout();
    }
}


// CUSTOMER PROFILE

async function loadCustomerProfile() {

    try {

        const data = await API.request(
            "/customer/profile",
            {
                method: "GET"
            }
        );


        State.accounts = data.accounts || [];

        let html = `

            <div class="profile-grid">

                <div class="profile-item">
                    <strong>Full Name</strong>
                    ${data.fullName || "-"}
                </div>

                <div class="profile-item">
                    <strong>Email</strong>
                    ${data.email || "-"}
                </div>

                <div class="profile-item">
                    <strong>Mobile</strong>
                    ${data.mobile || "-"}
                </div>

                <div class="profile-item">
                    <strong>KYC Status</strong>
                    ${data.kycStatus || "-"}
                </div>

            </div>

            <h3 style="margin-top:25px;">
                My Accounts
            </h3>

            <div class="account-list">
        `;


        if (State.accounts.length === 0) {

            html += `
                <p>No accounts found.</p>
            `;

        } else {

            State.accounts.forEach(account => {

                html += `

                    <div class="account-card">

                        <h3>
                            ${account.accountType}
                        </h3>

                        <p>
                            Account:
                            ${account.accountNumber}
                        </p>

                        <p>
                            Status:
                            ${account.status}
                        </p>

                        <div class="balance">
                            ₹${Number(account.balance).toFixed(2)}
                        </div>

                    </div>

                `;
            });
        }


        html += `
            </div>
        `;


        $("customerProfile").innerHTML = html;


        populateAccountSelects();

    } catch (error) {

        $("customerProfile").textContent =
            "Unable to load profile.";

        showToast(
            error.message,
            "error"
        );
    }
}



// ACCOUNT SELECTION

function populateAccountSelects() {

    const transferSelect =
        $("transferFromAccount");

    const loanSelect =
        $("loanAccount");


    transferSelect.innerHTML =
        '<option value="">Select account</option>';

    loanSelect.innerHTML =
        '<option value="">Select account</option>';


    State.accounts.forEach(account => {

        const option1 =
            document.createElement("option");

        option1.value = account.accountNumber;

        option1.textContent =
            `${account.accountNumber} - ₹${Number(account.balance).toFixed(2)}`;

        transferSelect.appendChild(option1);


        const option2 =
            document.createElement("option");

        option2.value = account.accountNumber;

        option2.textContent =
            `${account.accountNumber} - ${account.accountType}`;

        loanSelect.appendChild(option2);
    });
}



//MONEY  TRANSFER


async function transferMoney(event) {

    event.preventDefault();

    const accountNumber =
        $("transferFromAccount").value;

    const toAccount =
        $("transferToAccount").value.trim();

    const amount =
        Number($("transferAmount").value);


    if (!accountNumber) {

        showToast(
            "Select the source account",
            "error"
        );

        return;
    }


    try {

        const data = await API.request(
            `/customer/accounts/${encodeURIComponent(accountNumber)}/transfer`,
            {
                method: "POST",

                body: JSON.stringify({
                    toAccount,
                    amount
                })
            }
        );


        showToast(
            data.message || "Transfer successful",
            "success"
        );


        $("transferForm").reset();

        loadCustomerProfile();

    } catch (error) {

        showToast(
            error.message || "Transfer failed",
            "error"
        );
    }
}



// LOAN APPLICATION

async function applyLoan(event) {

    event.preventDefault();

    const accountNumber =
        $("loanAccount").value;

    const loanType =
        $("loanType").value;

    const amount =
        Number($("loanAmount").value);

    const tenureMonths =
        Number($("loanTenure").value);


    if (!accountNumber) {

        showToast(
            "Select an account",
            "error"
        );

        return;
    }


    try {

        const data = await API.request(
            `/customer/accounts/${encodeURIComponent(accountNumber)}/loans/apply`,
            {
                method: "POST",

                body: JSON.stringify({
                    loanType,
                    amount,
                    tenureMonths
                })
            }
        );


        showToast(
            data.message || "Loan application submitted",
            "success"
        );


        $("loanForm").reset();

    } catch (error) {

        showToast(
            error.message || "Loan application failed",
            "error"
        );
    }
}


// SIGNUP


async function signupCustomer(event) {

    event.preventDefault();


    const fullName =
        $("signupFullName").value.trim();

    const email =
        $("signupEmail").value.trim();

    const mobile =
        $("signupMobile").value.trim();

    const initialAccountType =
        $("signupAccountType").value;


    if (!fullName ||
        !email ||
        !mobile ||
        !initialAccountType) {

        showToast(
            "Please fill all fields",
            "error"
        );

        return;
    }


    try {

        const data = await API.request(
            "/clerk/customers/initiate",
            {
                method: "POST",

                body: JSON.stringify({
                    fullName,
                    email,
                    mobile,
                    initialAccountType
                })
            }
        );


        showToast(
            data.message ||
            "Registration submitted successfully. Please wait for admin approval.",
            "success"
        );


        $("signupForm").reset();

        setTimeout(() => {
            showLogin();
        }, 1500);

    } catch (error) {

        showToast(
            error.message ||
            "Unable to create account",
            "error"
        );
    }
}


// FORGOT PASSWORD - SEND OTP


async function sendOtp(event) {

    event.preventDefault();

    const identifier =
        $("forgotIdentifier").value.trim();


    try {

        const data = await API.request(
            `/auth/otp/send?identifier=${encodeURIComponent(identifier)}`,
            {
                method: "POST"
            }
        );


        State.resetIdentifier = identifier;


        showToast(
            data.message || "OTP sent successfully",
            "success"
        );


        $("otpSection")
            .classList.remove("hidden");

    } catch (error) {

        showToast(
            error.message || "Unable to send OTP",
            "error"
        );
    }
}


// VERIFY OTP


async function verifyOtp(event) {
    event.preventDefault();
    const otpCode = $("otpCode").value.trim();

    try {
        await API.request("/auth/otp/verify", {
            method: "POST",
            body: JSON.stringify({
                emailOrAccountNumber: State.resetIdentifier,
                otpCode: otpCode // Match the DTO
            })
        });

        // Save the actual OTP code instead of looking for a non-existent token
        State.verifiedOtpCode = otpCode; 

        showToast("OTP verified", "success");
        $("resetPasswordSection").classList.remove("hidden");
    } catch (error) {
        showToast(error.message || "Invalid OTP", "error");
    }
}


// RESET PASSWORD


async function resetPassword(event) {
    event.preventDefault();

    const newPassword = $("newPassword").value;

    try {
        const data = await API.request("/auth/reset-password", {
            method: "POST",
            body: JSON.stringify({
                emailOrAccountNumber: State.resetIdentifier,
                otpCode: State.verifiedOtpCode, 
                newPassword: newPassword
            })
        });

        showToast(data.message || "Password reset successful", "success");
        
        $("resetPasswordForm").reset();
        setTimeout(() => { showLogin(); }, 1500);

    } catch (error) {
        showToast(error.message || "Password reset failed", "error");
    }
}



// CLERK - CUSTOMER CREATION

async function clerkCreateCustomer(event) {

    event.preventDefault();


    const request = {

        fullName:
            $("clerkFullName").value.trim(),

        email:
            $("clerkEmail").value.trim(),

        mobile:
            $("clerkMobile").value.trim(),

        initialAccountType:
            $("clerkAccountType").value
    };


    try {

        const data = await API.request(
            "/clerk/customers/initiate",
            {
                method: "POST",

                body: JSON.stringify(request)
            }
        );


        showToast(
            data.message ||
            "Customer registered successfully",
            "success"
        );


        $("clerkCustomerForm").reset();

    } catch (error) {

        showToast(
            error.message ||
            "Customer registration failed",
            "error"
        );
    }
}




async function loadAdminData() {

    await Promise.all([
        loadPendingCustomers(),
        loadPendingLoans()
    ]);
}



async function loadPendingCustomers() {

    try {

        const customers =
            await API.request(
                "/admin/customers/pending",
                {
                    method: "GET"
                }
            );


        const container =
            $("pendingCustomers");


        if (!customers || customers.length === 0) {

            container.innerHTML =
                "<p>No pending customers.</p>";

            return;
        }


        container.innerHTML = "";


        customers.forEach(customer => {

            const div =
                document.createElement("div");

            div.className = "admin-item";


            let accounts = "";

            if (customer.accounts) {

                customer.accounts.forEach(account => {

                    accounts += `
                        <p>
                            Account Type:
                            ${account.accountType}
                        </p>

                        <p>
                            Status:
                            ${account.status}
                        </p>
                    `;
                });
            }


            div.innerHTML = `

    <p>
        <strong>Name:</strong>
        ${customer.fullName}
    </p>

    <p>
        <strong>Email:</strong>
        ${customer.email}
    </p>

    <p>
        <strong>Mobile:</strong>
        ${customer.mobile}
    </p>

    <p>
        <strong>KYC:</strong>
        ${customer.kycStatus}
    </p>

    ${accounts}

    <button
        onclick="reviewCustomer(${customer.id}, 'APPROVE')">
        Approve Customer
    </button>

    <button
        onclick="reviewCustomer(${customer.id}, 'REJECT')">
        Reject Customer
    </button>
`;


            container.appendChild(div);
        });

    } catch (error) {

        $("pendingCustomers").innerHTML =
            "<p>Unable to load pending customers.</p>";

        showToast(
            error.message,
            "error"
        );
    }
}


async function reviewCustomer(customerId, action, reason = "") {

    try {

        const data = await API.request(
            `/admin/customers/${customerId}/review`,
            {
                method: "POST",
                body: JSON.stringify({
                    action: action,
                    reason: reason
                })
            }
        );

        showToast(
            data.message ||
            `Customer ${action.toLowerCase()}d successfully`,
            "success"
        );

        await loadPendingCustomers();

    } catch (error) {

        console.error("Review customer error:", error);

        showToast(
            error.message || "Customer review failed",
            "error"
        );
    }
}



async function loadPendingLoans() {

    try {

        const loans =
            await API.request(
                "/admin/loans/pending",
                {
                    method: "GET"
                }
            );


        const container =
            $("pendingLoans");


        if (!loans || loans.length === 0) {

            container.innerHTML =
                "<p>No pending loans.</p>";

            return;
        }


        container.innerHTML = "";


        loans.forEach(loan => {

            const div =
                document.createElement("div");

            div.className = "admin-item";


            div.innerHTML = `

                <p>
                    <strong>Loan ID:</strong>
                    ${loan.id}
                </p>

                <p>
                    <strong>Account:</strong>
                    ${loan.accountNumber}
                </p>

                <p>
                    <strong>Loan Type:</strong>
                    ${loan.loanType}
                </p>

                <p>
                    <strong>Amount:</strong>
                    ₹${Number(loan.amount).toFixed(2)}
                </p>

                <p>
                    <strong>Tenure:</strong>
                    ${loan.tenureMonths} months
                </p>

                <p>
                    <strong>Interest:</strong>
                    ${loan.interestRate}%
                </p>

                <p>
                    <strong>Status:</strong>
                    ${loan.status}
                </p>

                <button
                    onclick="approveLoan(${loan.id})">
                    Approve Loan
                </button>
            `;


            container.appendChild(div);
        });

    } catch (error) {

        $("pendingLoans").innerHTML =
            "<p>Unable to load pending loans.</p>";

        showToast(
            error.message,
            "error"
        );
    }
}



async function approveLoan(loanId) {

    try {

        const data = await API.request(
            `/admin/loans/${loanId}/approve`,
            {
                method: "POST"
            }
        );


        showToast(
            data.message ||
            "Loan approved successfully",
            "success"
        );


        loadPendingLoans();

    } catch (error) {

        showToast(
            error.message ||
            "Loan approval failed",
            "error"
        );
    }
}


// ======================================================
// LOGOUT
// ======================================================

function logout() {

    localStorage.removeItem("token");
    localStorage.removeItem("role");
    localStorage.removeItem("username");

    State.token = null;
    State.role = null;
    State.username = null;
    State.accounts = [];

    $("dashboardSection").classList.add("hidden");
    $("navUser").classList.add("hidden");

    $("authSection").classList.remove("hidden");

    showLogin();

    $("loginForm").reset();
}


// ======================================================
// EVENT LISTENERS
// ======================================================

$("loginForm")
    .addEventListener("submit", login);


$("signupForm")
    .addEventListener("submit", signupCustomer);


$("transferForm")
    .addEventListener("submit", transferMoney);


$("loanForm")
    .addEventListener("submit", applyLoan);


$("clerkCustomerForm")
    .addEventListener("submit", clerkCreateCustomer);


$("sendOtpForm")
    .addEventListener("submit", sendOtp);


$("verifyOtpForm")
    .addEventListener("submit", verifyOtp);


$("resetPasswordForm")
    .addEventListener("submit", resetPassword);


$("showSignupBtn")
    .addEventListener("click", showSignup);


$("showLoginBtn")
    .addEventListener("click", showLogin);


$("forgotPasswordBtn")
    .addEventListener("click", showForgotPassword);


$("backToLoginBtn")
    .addEventListener("click", showLogin);


$("logoutBtn")
    .addEventListener("click", logout);


// ======================================================
// AUTO LOGIN
// ======================================================

if (State.token && State.role) {

    showDashboard(State.role);

}